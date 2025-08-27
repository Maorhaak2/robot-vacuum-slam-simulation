package bgu.spl.mics.application.objects;
import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    private String id;
    private String Description;
    private List<CloudPoint> Coordinates;

    public LandMark(String id, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.Description = description;
        this.Coordinates = coordinates;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return Description;
    }

    public List<CloudPoint> getCoordinates() {
        return Coordinates;
    }

    public synchronized void updateCoordinates(List<CloudPoint> newPoints) {
        for (int i = 0; i < newPoints.size(); i++) {
            if (i < Coordinates.size()) {
                double avgX = (Coordinates.get(i).getX() + newPoints.get(i).getX()) / 2;
                double avgY = (Coordinates.get(i).getY() + newPoints.get(i).getY()) / 2;
                Coordinates.set(i, new CloudPoint(avgX, avgY));
            } else {
                Coordinates.add(newPoints.get(i));
            }
        }
    }

}
