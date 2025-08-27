package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.List;
import bgu.spl.mics.application.configs.Configuration;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private STATUS status;
    private int currentTick;
    private List<Pose> PoseList;

      public GPSIMU(STATUS status, int currentTick) {
        this.status = status;
        this.currentTick = currentTick;
        this.PoseList = new ArrayList<>();
        initializePoseList();
    }
    public boolean isLastTick(int currentTick) {
        if (PoseList.isEmpty()) {
            return false; // No poses available
        }
        // Get the last pose's time
        int lastPoseTime = PoseList.get(PoseList.size() - 1).getTime();
        return currentTick >= lastPoseTime;
    }

    public STATUS getStatus() {
        return status;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public List<Pose> getPoseList() {
        return PoseList;
    }


    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    public void setPoseList(List<Pose> poseList) {
        this.PoseList = poseList;
    }

    public void addPose(Pose pose) {
        this.PoseList.add(pose);
    }

    public Pose getLatestPose() {
        if (PoseList.isEmpty()) {
            return null;
        }
        return PoseList.get(PoseList.size() - 1);
    }

    @Override
    public String toString() {
        return "GPSIMU{" +
                "status=" + status +
                ", currentTick=" + currentTick +
                ", poseList=" + PoseList +
                '}';
    }
    private void initializePoseList() {
        try {
            Configuration config = Configuration.getInstance(null); 
            this.PoseList = config.loadPoseList(); 
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize PoseList in GPSIMU", e);
        }
    }

}
