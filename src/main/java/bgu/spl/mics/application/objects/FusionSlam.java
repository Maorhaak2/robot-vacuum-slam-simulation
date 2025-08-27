package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private static List<LandMark> landMarks;
    private static List<Pose> poses;

    private final AtomicInteger activeMicroservicesCount;
    private final int durationTime;
    private volatile boolean outputGenerated;

    // Private constructor
    private FusionSlam(int initialMicroservicesCount, int durationTime) {
        landMarks = new ArrayList<>();
        poses = new ArrayList<>();
        this.activeMicroservicesCount = new AtomicInteger(initialMicroservicesCount);
        this.durationTime = durationTime;
        this.outputGenerated = false;
    }

    // Holder with lazy initialization
    private static class Holder {
        private static FusionSlam instance;

        private static FusionSlam getInstance(int initialMicroservicesCount, int durationTime) {
            if (instance == null) {
                instance = new FusionSlam(initialMicroservicesCount, durationTime);
            }
            return instance;
        }
    }

    public static FusionSlam getInstance(int initialMicroservicesCount, int durationTime) {
        return Holder.getInstance(initialMicroservicesCount, durationTime);
    }

    // Non-synchronized methods
    public void addLandMark(LandMark landMark) {
        landMarks.add(landMark);
    }

    public void addPose(Pose pose) {
        poses.add(pose);
    }

    public List<LandMark> getLandMarks() {
        return landMarks;
    }

    public List<Pose> getPoses() {
        return poses;
    }

    public Pose getPoseAtTime(int time) {
        for (Pose pose : poses) {
            if (pose.getTime() == time) {
                return pose;
            }
        }
        return null;
    }

    public LandMark findExistingLandmark(String id) {
        for (LandMark landmark : landMarks) {
            if (landmark.getId().equals(id)) {
                return landmark;
            }
        }
        return null;
    }

    public void processTrackedObjects(List<TrackedObject> trackedObjects, int detectionTime) {
        Pose poseAtTime = getPoseAtTime(detectionTime);
        if (poseAtTime == null) {
            System.out.println("Pose not found for time: " + detectionTime);
            return;
        }

        for (TrackedObject object : trackedObjects) {
            List<CloudPoint> transformedPoints = new ArrayList<>();

            for (CloudPoint point : object.getCoordinates()) {
                double radYaw = Math.toRadians(poseAtTime.getYaw());
                double cosTheta = Math.cos(radYaw);
                double sinTheta = Math.sin(radYaw);

                double globalX = cosTheta * point.getX() - sinTheta * point.getY() + poseAtTime.getX();
                double globalY = sinTheta * point.getX() + cosTheta * point.getY() + poseAtTime.getY();

                transformedPoints.add(new CloudPoint(globalX, globalY));
            }

            LandMark existingLandmark = findExistingLandmark(object.getId());
            if (existingLandmark == null) {
                addLandMark(new LandMark(object.getId(), object.getDescription(), transformedPoints));
                StatisticalFolder.getInstance().addLandmarks(1);
            } else {
                existingLandmark.updateCoordinates(transformedPoints);
            }
        }
    }

    public boolean decrementMicroserviceCount() {
        int remaining = activeMicroservicesCount.decrementAndGet();
        System.out.println("FusionSlam: Active microservices remaining: " + remaining);
        if(checkTerminationConditions()){
            return true;
        }
        return false;
    }

    public boolean handleTickBroadcast(int currentTime) {
        if (currentTime >= durationTime && !outputGenerated) {
            generateOutputFile();
            return true;
        }
        return false;
    }

    public boolean checkTerminationConditions() {
        if (activeMicroservicesCount.get() == 0 && !outputGenerated) {
            generateOutputFile();
            return true;
        }
        return false;
    }

     private void generateOutputFile() {
        if (outputGenerated) return;
        outputGenerated = true;
        System.out.println("FusionSlam: Generating output file...");

        try (FileWriter writer = new FileWriter("output_file.json")) {
            writer.write("{\"systemRuntime\":" + StatisticalFolder.getInstance().getSystemRuntime() +
                        ",\"numDetectedObjects\":" + StatisticalFolder.getInstance().getNumDetectedObjects() +
                        ",\"numTrackedObjects\":" + StatisticalFolder.getInstance().getNumTrackedObjects() +
                        ",\"numLandmarks\":" + StatisticalFolder.getInstance().getNumLandmarks() + ",");

            writer.write("\n");
            writer.write("\"landMarks\":{\n");
            for (int i = 0; i < landMarks.size(); i++) {
                LandMark landMark = landMarks.get(i);
                writer.write("\"" + landMark.getId() + "\":{\"id\":\"" + landMark.getId() +
                            "\",\"description\":\"" + landMark.getDescription() +
                            "\",\"coordinates\":" + new Gson().toJson(landMark.getCoordinates()) + "}");
                if (i < landMarks.size() - 1) {
                    writer.write(",\n"); 
                }
            }
            writer.write("\n}}");
            System.out.println("FusionSlam: Output file generated successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateErrorOutputFile(String error, String faultySensor) {
        if (outputGenerated) return;
        outputGenerated = true;
        System.out.println("FusionSlam: Generating error output file...");

        try (FileWriter writer = new FileWriter("output_file.json")) {
            writer.write("{\n");

            // Print error and faulty sensor
            writer.write("  \"error\": \"" + error + "\",\n");
            writer.write("  \"faultySensor\": \"" + faultySensor + "\",\n");

            // Print lastCamerasFrame
            writer.write("  \"lastCamerasFrame\": {\n");
            Map<Integer, StampedDetectedObjects> cameraFrames = LastFrames.getInstance().getAllCameraFrames();
            int cameraCount = 0;
            for (Map.Entry<Integer, StampedDetectedObjects> entry : cameraFrames.entrySet()) {
                writer.write("    \"Camera" + entry.getKey() + "\": " + new Gson().toJson(entry.getValue()));
                if (++cameraCount < cameraFrames.size()) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            writer.write("  },\n");

            // Print lastLiDarWorkerTrackersFrame
            writer.write("  \"lastLiDarWorkerTrackersFrame\": {\n");
            Map<Integer, List<TrackedObject>> lidarFrames = LastFrames.getInstance().getAllLiDarFrames();
            int lidarCount = 0;
            for (Map.Entry<Integer, List<TrackedObject>> entry : lidarFrames.entrySet()) {
                writer.write("    \"LiDarWorkerTracker" + entry.getKey() + "\": " + new Gson().toJson(entry.getValue()));
                if (++lidarCount < lidarFrames.size()) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            writer.write("  },\n");

            // Print poses
            writer.write("  \"poses\": [");
            for (int i = 0; i < poses.size(); i++) {
                Pose pose = poses.get(i);
                writer.write("{\"time\": " + pose.getTime() + 
                            ", \"x\": " + pose.getX() + 
                            ", \"y\": " + pose.getY() + 
                            ", \"yaw\": " + pose.getYaw() + "}");
                if (i < poses.size() - 1) {
                    writer.write(",");
                }
            }
            writer.write("  ],\n");

            // Print statistics
            writer.write("  \"statistics\": {\n");
            writer.write("    \"systemRuntime\": " + StatisticalFolder.getInstance().getSystemRuntime() + ",\n");
            writer.write("    \"numDetectedObjects\": " + StatisticalFolder.getInstance().getNumDetectedObjects() + ",\n");
            writer.write("    \"numTrackedObjects\": " + StatisticalFolder.getInstance().getNumTrackedObjects() + ",\n");
            writer.write("    \"numLandmarks\": " + StatisticalFolder.getInstance().getNumLandmarks() + ",\n");

            // Print landmarks
            writer.write("    \"landMarks\": {\n");
            for (int i = 0; i < landMarks.size(); i++) {
                LandMark landMark = landMarks.get(i);
                writer.write("      \"" + landMark.getId() + "\": " + new Gson().toJson(landMark));
                if (i < landMarks.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            writer.write("    }\n");
            writer.write("  }\n");

            writer.write("}");
            System.out.println("FusionSlam: Error output file generated successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}