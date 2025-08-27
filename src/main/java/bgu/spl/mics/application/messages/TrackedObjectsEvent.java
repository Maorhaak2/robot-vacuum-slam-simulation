package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;
import java.util.List;

public class TrackedObjectsEvent implements Event<Void> {
    private final List<TrackedObject> trackedObjects;
    int time;

    public TrackedObjectsEvent(List<TrackedObject> trackedObjects, int time) {
        this.trackedObjects = trackedObjects;
        this.time = time;
    }

    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }

    public int getTime(){
        return time;
    }
}