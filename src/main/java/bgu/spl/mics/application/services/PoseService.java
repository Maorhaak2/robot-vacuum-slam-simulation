package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;


/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {

    private final GPSIMU gpsimu;
    private int currentTick;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
        currentTick = 0;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " started");

        this.subscribeBroadcast(TickBroadcast.class, tick -> {
            currentTick = tick.getTick(); 
            gpsimu.setCurrentTick(currentTick);
            
            if (gpsimu.isLastTick(currentTick)) {
                System.out.println(getName() + ": Reached the last tick. Sending TerminatedBroadcast and terminating.");
                gpsimu.setStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
                return;
            }
            
            Pose currentPose = gpsimu.getPoseList().get(currentTick - 1);
            if (gpsimu.getStatus() != STATUS.UP && currentPose != null) {
                System.out.println(getName() + ": No pose data available at tick " + tick.getTick());
            }
            else {
                this.sendEvent(new PoseEvent(currentPose));
            }
        });

        this.subscribeBroadcast(CrashedBroadcast.class, crashed-> {
            System.out.println((getName() + ": Received CrashedBroadcast from " + crashed.getMsName()));
            gpsimu.setStatus(STATUS.ERROR);
            terminate();
        });

        this.subscribeBroadcast(TerminatedBroadcast.class, terminated-> {
            System.out.println(getName() + ": Received TerminatedBroadcast");
            if(terminated.getMsName() == "TimeService") {
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }  
        });
    }
}
