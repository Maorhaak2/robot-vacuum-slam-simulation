package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

public class DetectObjectsEvent implements Event<Boolean> {
    private final StampedDetectedObjects stampedDetectedObjects;

    public DetectObjectsEvent(StampedDetectedObjects stampedDetectedObjects) {
        this.stampedDetectedObjects = stampedDetectedObjects;
    }

    public StampedDetectedObjects getStampedDetectedObjects() {
        return stampedDetectedObjects;
    }
}