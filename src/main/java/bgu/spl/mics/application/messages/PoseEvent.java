package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;

public class PoseEvent implements Event<Pose> {
    private final Pose currentPose; 

    public PoseEvent(Pose currentPose) {
        this.currentPose = currentPose;
    }

    public Pose getCurrentPose() {
        return currentPose;
    }
}