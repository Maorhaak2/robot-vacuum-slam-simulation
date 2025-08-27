package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {

    private final Map<Integer, List<StampedCloudPoints>> data = new HashMap<>(); // time -> list of StampedCloudPoints
    private int lastTime = 0; 

    private static class Holder {
        private static final LiDarDataBase instance = new LiDarDataBase();
    }

    private LiDarDataBase() {}

    public static LiDarDataBase getInstance() {
        return Holder.instance;
    }

    /**
     * Loads LiDAR data from a JSON file.
     *
     * @param filePath Path to the JSON file.
     */
    public void loadData(String filePath) {
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            try (FileReader reader = new FileReader(filePath)) {
                List<Map<String, Object>> records = gson.fromJson(reader, listType);

                for (Map<String, Object> record : records) {
                    int time = ((Double) record.get("time")).intValue();
                    String id = (String) record.get("id");
                    List<List<Double>> cloudPointsRaw = (List<List<Double>>) record.get("cloudPoints");
                    List<CloudPoint> cloudPoints = new ArrayList<>();
                    
                    for (List<Double> point : cloudPointsRaw) {
                        double x = point.get(0);
                        double y = point.get(1);
                        cloudPoints.add(new CloudPoint(x, y));
                    }

                    StampedCloudPoints stampedCloudPoint = new StampedCloudPoints(id, time, cloudPoints);
                    data.putIfAbsent(time, new ArrayList<>());
                    data.get(time).add(stampedCloudPoint);

                    if (time > lastTime) {
                        lastTime = time;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading LiDar data: " + e.getMessage());
        }
    }

    /**
     * Returns the stamped cloud points for a specific time.
     *
     * @param time The time for which to retrieve the data.
     * @return A list of stamped cloud points.
     */
    public List<StampedCloudPoints> getStampedCloudPointsAtTime(int time) {
        return data.getOrDefault(time, Collections.emptyList());
    }

    /**
     * Returns the last time for which data exists in the database.
     *
     * @return The last time.
     */
    public int getLastTime() {
        return lastTime;
    }
}