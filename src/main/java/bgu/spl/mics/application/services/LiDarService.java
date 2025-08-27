package bgu.spl.mics.application.services;

import java.util.ArrayList;
import java.util.List;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.LastFrames;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;


/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    private final LiDarWorkerTracker lidar;
    private final LiDarDataBase lidarDatabase = LiDarDataBase.getInstance(); 
    private int currentTick;
    private final List<TrackedObject> pendingObjects = new ArrayList<>();
    private LastFrames lidarLastFrames;
    
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("Lidar Worker" + Integer.toString(LiDarWorkerTracker.getId()));
        lidar = LiDarWorkerTracker;
        currentTick = 0;
        lidarLastFrames = LastFrames.getInstance();
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " started");

       this.subscribeBroadcast(TerminatedBroadcast.class, terminated-> {
            System.out.println(getName() + ": Received TerminatedBroadcast" + terminated.getMsName());
            if(terminated.getMsName() == "TimeService") {
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }  
        });

        this.subscribeBroadcast(CrashedBroadcast.class, crashed-> {
            System.out.println((getName() + ": Received CrashedBroadcast from " + crashed.getMsName()));
            lidar.setStatus(STATUS.ERROR);
            terminate();
        });

        this.subscribeBroadcast(TickBroadcast.class, tick -> {
            currentTick = tick.getTick(); 
            if (lidarDatabase.getLastTime() + lidar.getFrequency() < currentTick) {
                lidar.setStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
                return;
            }

            if (lidar.checkForError(currentTick, lidarDatabase)) {
                System.out.println(getName() + ": ERROR detected. Sending CrashedBroadcast and terminating.");
                sendBroadcast(new CrashedBroadcast(getName(), "Sensor " + getName() + " disconnected"));
                terminate();
                return;
            }

            List<TrackedObject> toSend = new ArrayList<>();
            for (TrackedObject object : pendingObjects) {
                if (currentTick >= object.getTime()+ lidar.getFrequency()) {
                    toSend.add(object);
                }
            }

            if (!toSend.isEmpty()) {
                StatisticalFolder.getInstance().addTrackedObjects(toSend.size());
                TrackedObjectsEvent trackedEvent = new TrackedObjectsEvent(toSend, toSend.get(0).getTime());
                this.sendEvent(trackedEvent);
                lidarLastFrames.updateLiDarFrame(lidar.getId(), lidar.getLastTrackedObjects());
                System.out.println(getName() + ": Sent TrackedObjectsEvent at tick " + currentTick);
                pendingObjects.removeAll(toSend);
            }  
        });


        this.subscribeEvent(DetectObjectsEvent.class, event -> {
            int detectionTime = event.getStampedDetectedObjects().getTime();
        
            List<TrackedObject> trackedObjects = lidar.processDetectedObjects(
                    event.getStampedDetectedObjects().getDetectedObjects(),
                    detectionTime,
                    lidarDatabase
            );
        
            if (!trackedObjects.isEmpty()) {
                if (currentTick >= detectionTime + lidar.getFrequency()) {
                    StatisticalFolder.getInstance().addTrackedObjects(trackedObjects.size());
                    TrackedObjectsEvent trackedEvent = new TrackedObjectsEvent(trackedObjects, detectionTime);
                    this.sendEvent(trackedEvent);
                    lidarLastFrames.updateLiDarFrame(lidar.getId(), lidar.getLastTrackedObjects());
                    System.out.println(getName() + ": Sent TrackedObjectsEvent at tick " + currentTick);
                } else {
                    pendingObjects.addAll(trackedObjects);
                }
            }
            //complete(event, true);
        });
    }
}
