package bgu.spl.mics.application.configs;

import com.google.gson.Gson;

import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.Pose;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * Represents the configuration data for the simulation.
 */
public class Configuration {


    private static Configuration instance;
    private CamerasConfig Cameras;
    private LidarWorkersConfig LiDarWorkers;
    private String poseJsonFile;
    private int TickTime;
    private int Duration;

    public static Configuration getInstance(String filePath) {
       if (instance == null) {
           try (FileReader reader = new FileReader(filePath)) {
               Gson gson = new Gson();

               instance = gson.fromJson(reader, Configuration.class);

               System.out.println("Configuration loaded successfully: " + instance);
   
               Path basePath = Paths.get(filePath).getParent();
               if (instance.getCameras().getCameraDatasPath() != null) {
                   instance.getCameras().setCameraDatasPath(basePath.resolve(instance.getCameras().getCameraDatasPath()).toString());
               }
               if (instance.getLidarWorkers().getLidarsDataPath() != null) {
                   instance.getLidarWorkers().setLidarsDataPath(basePath.resolve(instance.getLidarWorkers().getLidarsDataPath()).toString());
               }
               if (instance.getPoseJsonFile() != null) {
                   instance.setPoseJsonFile(basePath.resolve(instance.getPoseJsonFile()).toString());
               }

           } catch (FileNotFoundException e) {
               throw new RuntimeException("Configuration file not found: " + filePath, e);
           } catch (IOException e) {
               throw new RuntimeException("Failed to read configuration file: " + filePath, e);
           }
       }

        return instance;
    }

    public CamerasConfig getCameras() {
        return Cameras;
    }

    public LidarWorkersConfig getLidarWorkers() {
        return LiDarWorkers;
    }

    public String getPoseJsonFile() {
        return poseJsonFile;
    }

    public int getTickTime() {
        return TickTime;
    }

    public int getDuration() {
        return Duration;
    }


    public void setPoseJsonFile(String poseJsonFile) {
        this.poseJsonFile = poseJsonFile;
    }

    public static class CamerasConfig {
        private List<CameraConfig> CamerasConfigurations;
        private String camera_datas_path;

        public List<CameraConfig> getCamerasConfigurations() {
            return CamerasConfigurations;
        }

        public String getCameraDatasPath() {
            return camera_datas_path;
        }
        public void setCameraDatasPath(String cameraDatasPath) {
            this.camera_datas_path = cameraDatasPath;
        }
    }

    public static class LidarWorkersConfig {
        private List<LidarConfig> LidarConfigurations;
        private String lidars_data_path;

        public List<LidarConfig> getLidarConfigurations() {
            return LidarConfigurations;
        }

        public String getLidarsDataPath() {
            return lidars_data_path;
        }
        public void setLidarsDataPath(String lidarsDataPath) {
            this.lidars_data_path = lidarsDataPath;
        }
    }

     public void initializeLiDarDataBase() {
        String lidarDataPath = getLidarWorkers().getLidarsDataPath();
        LiDarDataBase.getInstance().loadData(lidarDataPath);
        System.out.println("LiDar database initialized.");
 
    }
    public List<Pose> loadPoseList() {
        try (FileReader reader = new FileReader(this.poseJsonFile)) { 
            Gson gson = new Gson();
            Type poseListType = new TypeToken<List<Pose>>() {}.getType();
            return gson.fromJson(reader, poseListType); 
        } catch (Exception e) {
            throw new RuntimeException("Failed to load pose list from file: " + this.poseJsonFile, e);
        }
    }

}
