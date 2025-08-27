package bgu.spl.mics.application.configs;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CameraConfiguration {
    public static List<Camera> getCameras() {
        Configuration config = Configuration.getInstance(null);
        List<Camera> cameras = new ArrayList<>();
        CamerasData camerasData = readCamerasData(config.getCameras().getCameraDatasPath() );
        for(CameraConfig c : config.getCameras().getCamerasConfigurations()){
            Camera camera = new Camera(c.getId(), c.getFrequency());
            List<StampedDetectedObjects> detectedObjects = camerasData.getCameras().get(c.getcamera_key());
            if (detectedObjects != null) {
                for (StampedDetectedObjects stampedObject : detectedObjects) {
                camera.addDetectedObject(stampedObject);
                }
            }   
        cameras.add(camera);
        }
        return cameras;
    }
    public static CamerasData readCamerasData(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();


            Type type = new TypeToken<Map<String, List<StampedDetectedObjects>>>() {}.getType();
            Map<String, List<StampedDetectedObjects>> cameras = gson.fromJson(reader, type);

            CamerasData camerasData = new CamerasData();
            camerasData.setCameras(cameras);

            return camerasData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
