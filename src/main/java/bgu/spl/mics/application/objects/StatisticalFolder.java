package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;
/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */

public class StatisticalFolder {

    // שדות הנתונים
    private final AtomicInteger systemRuntime = new AtomicInteger(0); 
    private final AtomicInteger numDetectedObjects = new AtomicInteger(0); 
    private final AtomicInteger numTrackedObjects = new AtomicInteger(0); 
    private final AtomicInteger numLandmarks = new AtomicInteger(0); 

    // Singleton
    private static class Holder {
        private static final StatisticalFolder instance = new StatisticalFolder();
    }

    private StatisticalFolder() {}

    public static StatisticalFolder getInstance() {
        return Holder.instance;
    }

    public void incrementSystemRuntime() {
        systemRuntime.incrementAndGet();
    }

    public void addDetectedObjects(int count) {
        numDetectedObjects.addAndGet(count);
    }

    public void addTrackedObjects(int count) {
        numTrackedObjects.addAndGet(count);
    }

    public void addLandmarks(int count) {
        numLandmarks.addAndGet(count);
    }

    public int getSystemRuntime() {
        return systemRuntime.get();
    }

    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    public int getNumLandmarks() {
        return numLandmarks.get();
    }

    public void printStatistics() {
        System.out.println("System Runtime: " + getSystemRuntime());
        System.out.println("Number of Detected Objects: " + getNumDetectedObjects());
        System.out.println("Number of Tracked Objects: " + getNumTrackedObjects());
        System.out.println("Number of Landmarks: " + getNumLandmarks());
    }
}
 