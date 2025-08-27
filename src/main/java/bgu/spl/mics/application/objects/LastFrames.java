package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LastFrames {
    private final Map<Integer, StampedDetectedObjects> camerasLastFrames;
    private final Map<Integer, List<TrackedObject>> lidarsLastFrames; // Change to TrackedObject list
    private final Object lidarLock = new Object(); // Lock for LiDAR operations

    private LastFrames() {
        camerasLastFrames = new ConcurrentHashMap<>();
        lidarsLastFrames = new ConcurrentHashMap<>();
    }

     // Singleton holder
     private static class Holder {
        private static final LastFrames INSTANCE = new LastFrames();
    }

    // Public method to get the instance
    public static LastFrames getInstance() {
        return Holder.INSTANCE;
    }


    // Update the last frame for a camera (No need for lock)
    public void updateCameraFrame(int cameraId, StampedDetectedObjects stampedDetectedObject) {
        camerasLastFrames.put(cameraId, stampedDetectedObject);
    }

    // Retrieve the last frame for a camera (No need for lock)
    public StampedDetectedObjects getCameraLastFrame(int cameraId) {
        return camerasLastFrames.get(cameraId);
    }

    // Update the last frame for a LiDAR (With lock)
    public void updateLiDarFrame(int lidarId, List<TrackedObject> trackedObjects) {
        synchronized (lidarLock) {
            lidarsLastFrames.put(lidarId, trackedObjects);
        }
    }

    // Retrieve the last frame for a LiDAR (With lock)
    public List<TrackedObject> getLiDarLastFrames(int lidarId) {
        synchronized (lidarLock) {
            return lidarsLastFrames.get(lidarId);
        }
    }

    // Retrieve all LiDAR frames (With lock)
    public Map<Integer, List<TrackedObject>> getAllLiDarFrames() {
        synchronized (lidarLock) {
            return new ConcurrentHashMap<>(lidarsLastFrames); // Return a copy to avoid modifications
        }
    }

    // Retrieve all camera frames (No need for lock)
    public Map<Integer, StampedDetectedObjects> getAllCameraFrames() {
        return new ConcurrentHashMap<>(camerasLastFrames); // Return a copy to avoid modifications
    }
}