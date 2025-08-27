package bgu.spl.mics.application.objects;
import java.io.ObjectInputFilter.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import bgu.spl.mics.application.messages.DetectObjectsEvent;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private final int id;
    private int frequency;
    private STATUS status;
    private final List<StampedDetectedObjects> detectedObjectsList;
    private final Map<Integer, StampedDetectedObjects> eventMap = new TreeMap<>();

    public Camera(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.detectedObjectsList = new ArrayList<>();
        this.status = STATUS.UP;
    }

    public List<DetectedObject> getDetectedObjectsAtTime(int currentTime) {
        for (StampedDetectedObjects sDetectedObjects : detectedObjectsList) {
            if (sDetectedObjects.getTime() == currentTime) {
                return sDetectedObjects.getDetectedObjects();
            }
        }
        return null;
    }

    public Map<Integer, StampedDetectedObjects> getEventMap() {
        return eventMap;
    }

    public String checkForError(int currentTime) {
        List<DetectedObject> detectedObjects = getDetectedObjectsAtTime(currentTime);
        if (detectedObjects != null) {
            for (DetectedObject object : detectedObjects) {
                if ("ERROR".equals(object.getId())) {
                    return object.getDescription();
                }
            }
        }
        return null;
    }

    public boolean checkAndTerminate(int currentTime) {
        int lastTime = detectedObjectsList.get(detectedObjectsList.size() - 1).getTime() + frequency;
        if (currentTime > lastTime) {
            return true;
        }
        return false;
    }

    public void addDetectedObject(StampedDetectedObjects object) {
        detectedObjectsList.add(object);
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public StampedDetectedObjects getStampedDetectedObjectsAtTime(int currentTime) {
        for (StampedDetectedObjects sDetectedObjects : detectedObjectsList) {
            if (sDetectedObjects.getTime() == currentTime) {
                return sDetectedObjects;
            }
        }
        return null;
    }

    public void updateEventMap(int currentTime) {
        StampedDetectedObjects detectedObjects = getStampedDetectedObjectsAtTime(currentTime);
    
        if (detectedObjects != null) {
            int eventTime = currentTime + frequency; 
            eventMap.put(eventTime, detectedObjects);
        }
    }
}