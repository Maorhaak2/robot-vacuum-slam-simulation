package bgu.spl.mics.application;
import bgu.spl.mics.application.configs.*;

import java.util.List;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;


/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        System.out.println("Starting simulation...");
        String configFilePath = args[0];
        int microServicesCnt = 0;
        try {
            // Initialize the configuration
            //Configuration config = Configuration.getInstance("example_input_2/configuration_file.json");
            //Configuration config = Configuration.getInstance("example input/configuration_file.json");
            //Configuration config = Configuration.getInstance("example_input_With_error/configuration_file.json");
            Configuration config = Configuration.getInstance(configFilePath);
            config.initializeLiDarDataBase();   
            // Initialize MessageBus (Singleton)
            MessageBusImpl messageBus = MessageBusImpl.getInstance();

            // Initialize PoseService
            GPSIMU gpsimu = new GPSIMU(STATUS.UP, 0);
            PoseService poseService = new PoseService(gpsimu);
            Thread poseThread = new Thread(poseService);
            microServicesCnt++;
            poseThread.start();

            // Initialize Cameras and Camera Services
            List<Camera> cameras = CameraConfiguration.getCameras();
            for (Camera camera : cameras) {
                CameraService cameraService = new CameraService(camera);
                Thread cameraThread = new Thread(cameraService);
                microServicesCnt++;
                cameraThread.start();
            }

            // Initialize LiDAR Worker Services
            for (LidarConfig lidarConfig : config.getLidarWorkers().getLidarConfigurations()) {
                LiDarWorkerTracker lidarTracker = new LiDarWorkerTracker(lidarConfig.getId(), lidarConfig.getFrequency());
                LiDarService lidarService = new LiDarService(lidarTracker);
                Thread lidarThread = new Thread(lidarService);
                microServicesCnt++;
                lidarThread.start();
            }

            // Initialize Fusion-SLAM Singleton
            FusionSlam fusionSlam = FusionSlam.getInstance(microServicesCnt, config.getDuration());

            // Initialize Fusion-SLAM Service
            FusionSlamService fusionSlamService = new FusionSlamService(fusionSlam);
            Thread fusionThread = new Thread(fusionSlamService);
            fusionThread.start();

            // Initialize TimeService
            TimeService timeService = new TimeService(config.getTickTime(), config.getDuration());
            Thread timeThread = new Thread(timeService);
            timeThread.start();

            // Wait for TimeService to finish
            timeThread.join();
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.out.println("Simulation interrupted.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
            System.out.println("Simulation interrupted by interruption.");
        }
    }
}          
