package bgu.spl.mics;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.TrackedObject;

class FusionSlamTest {

    private FusionSlam fusionSlam;

    @BeforeEach
    void setUp() {
        // Initialize FusionSlam with 3 microservices and duration of 10
        fusionSlam = FusionSlam.getInstance(3, 10);
    }

    @Test
    void testProcessTrackedObjects() {
        // @PRE-CONDITION: FusionSlam is initialized and empty. A Pose exists for the given detectionTime.
        // @POST-CONDITION: Tracked objects are correctly transformed to landmarks and stored.

        // Initialize FusionSlam
        FusionSlam fusionSlam = FusionSlam.getInstance(0, 10);

        // Add a Pose for the detectionTime
        Pose testPose = new Pose(0, 0, 90, 1); // Pose at time 1 with 90 degrees rotation
        fusionSlam.addPose(testPose);

        // Create tracked objects
        List<CloudPoint> trackedCoordinates = List.of(new CloudPoint(1.0, 2.0), new CloudPoint(3.0, 4.0));
        List<TrackedObject> trackedObjects = List.of(
            new TrackedObject("Object1", 1, "Description1", trackedCoordinates),
            new TrackedObject("Object2", 1, "Description2", trackedCoordinates)
        );

        // Process the tracked objects
        fusionSlam.processTrackedObjects(trackedObjects, 1);

        // Verify that landmarks are created
        List<LandMark> landmarks = fusionSlam.getLandMarks();
        assertEquals(2, landmarks.size(), "There should be two landmarks created.");

        // Verify details of the first landmark
        LandMark landmark1 = landmarks.get(0);
        assertEquals("Object1", landmark1.getId(), "The ID of the first landmark should match the tracked object.");
        assertEquals("Description1", landmark1.getDescription(), "The description of the first landmark should match.");
        assertEquals(2, landmark1.getCoordinates().size(), "The first landmark should have 2 coordinates.");

        // Verify details of the second landmark
        LandMark landmark2 = landmarks.get(1);
        assertEquals("Object2", landmark2.getId(), "The ID of the second landmark should match the tracked object.");
        assertEquals("Description2", landmark2.getDescription(), "The description of the second landmark should match.");
        assertEquals(2, landmark2.getCoordinates().size(), "The second landmark should have 2 coordinates.");
    }
}