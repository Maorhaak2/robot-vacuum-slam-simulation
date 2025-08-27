package bgu.spl.mics.application.services;
import java.util.List;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.StatisticalFolder;


/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private final FusionSlam fusionSlam;

    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlamService");
        this.fusionSlam = fusionSlam;
    }

    @Override
    protected void initialize() {
        System.out.println(getName() + " initialized.");

        // Subscribe to TrackedObjectsEvent
        this.subscribeEvent(TrackedObjectsEvent.class, event -> {
            int detectionTime = event.getTime();
            List<TrackedObject> trackedObjects = event.getTrackedObjects();

            System.out.println(getName() + ": Processing TrackedObjectsEvent at time " + detectionTime);
            fusionSlam.processTrackedObjects(trackedObjects, detectionTime);
        });

        //suscribe to PoseEvent
        this.subscribeEvent(PoseEvent.class, poseEvent -> {
            Pose currentPose = poseEvent.getCurrentPose();
            if (currentPose != null) {
                System.out.println(getName() + ": Received PoseEvent at time " + currentPose.getTime());
                fusionSlam.addPose(currentPose); 
            }
        });

        // Subscribe to TickBroadcast
        this.subscribeBroadcast(TickBroadcast.class, tick -> {
            int currentTime = tick.getTick();
            StatisticalFolder.getInstance().incrementSystemRuntime();
            if(fusionSlam.handleTickBroadcast(currentTime)){
                this.sendBroadcast(new TerminatedBroadcast(this.getName()));
                terminate();
            }
        });

        // Subscribe to TerminatedBroadcast
        this.subscribeBroadcast(TerminatedBroadcast.class, terminated -> {
            System.out.println(getName() + ": Received TerminatedBroadcast. Updating FusionSlam." + terminated.getMsName());
            if(fusionSlam.decrementMicroserviceCount()){
                this.sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
        });

        // Subscribe to CrashedBroadcast
         this.subscribeBroadcast(CrashedBroadcast.class, crashed-> {
            System.out.println((getName() + ": Received CrashedBroadcast from " + crashed.getMsName()));
            fusionSlam.generateErrorOutputFile(crashed.getErrorMsg(), crashed.getMsName());
            this.sendBroadcast(new TerminatedBroadcast(getName()));
            terminate();
        });
    }
}
