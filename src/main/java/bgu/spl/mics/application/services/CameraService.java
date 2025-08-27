package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.*;

import java.io.ObjectInputFilter.Status;
import java.util.ArrayList;
import java.util.List;
import bgu.spl.mics.Future;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Event;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;



/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    private final Camera cam;
    private LastFrames camLastFrames;
    public CameraService(Camera camera) {
        super("Camera" + Integer.toString(camera.getId()));
        cam = camera;
        camLastFrames = LastFrames.getInstance();
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {

        System.out.println(getName() + " started");
        subscribeBroadcast(TickBroadcast.class, tick -> {
            int currentTime = tick.getTick();
            
            // Check if camera should terminate
            if (cam.checkAndTerminate(currentTime)) {
                System.out.println(getName() + ": No more objects to detect. Sending TerminatedBroadcast and terminating.");
                cam.setStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
                return;
            }
            
            cam.updateEventMap(currentTime);
            StampedDetectedObjects eventToSend = cam.getEventMap().remove(currentTime);
            String errorDescription = cam.checkForError(currentTime);
            if (eventToSend != null && errorDescription == null) {
                int numObjects = eventToSend.getDetectedObjects().size();
                StatisticalFolder.getInstance().addDetectedObjects(numObjects);
                sendEvent(new DetectObjectsEvent(eventToSend));
                camLastFrames.updateCameraFrame(cam.getId(), eventToSend);
                System.out.println(getName() + ": Sent DetectObjectsEvent for time " + currentTime + ", detection time: " + eventToSend.getTime());
            }            
            // Check for errors in the camera
            if (errorDescription != null) {
                System.out.println(getName() + ": ERROR detected. Sending CrashedBroadcast and terminating.");
                sendBroadcast(new CrashedBroadcast(getName(), errorDescription));
                terminate();
                return;
            }

            
        });

        this.subscribeBroadcast(TerminatedBroadcast.class, terminated-> {
            System.out.println(getName() + ": Received TerminatedBroadcast " + terminated.getMsName());
            if(terminated.getMsName() == "TimeService") {
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }       
        });

        this.subscribeBroadcast(CrashedBroadcast.class, crashed-> {
            System.out.println((getName() + ": Received CrashedBroadcast from: " + crashed.getMsName()));
            cam.setStatus(STATUS.ERROR);
            terminate();
        });
    }  
}
