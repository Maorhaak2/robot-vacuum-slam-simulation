package bgu.spl.mics.application.configs;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import java.util.List;
import java.util.Map;
public class CamerasData {
    private Map<String, List<StampedDetectedObjects>> cameras;

    public Map<String, List<StampedDetectedObjects>> getCameras() {
        return cameras;
    }

    public void setCameras(Map<String, List<StampedDetectedObjects>> cameras) {
        this.cameras = cameras;
    }
}

