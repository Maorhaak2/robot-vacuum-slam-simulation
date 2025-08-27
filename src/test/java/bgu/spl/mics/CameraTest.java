package bgu.spl.mics;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.objects.Camera;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CameraTest {

    private Camera camera;

    @BeforeEach
    public void setUp() {
        camera = new Camera(1, 5); 
    }

    @Test
    public void testGetDetectedObjectsAtTime() {
        // @PRE-CONDITION: Camera contains detected objects at a specific time.
        // @POST-CONDITION: The correct list of detected objects is returned.

        List<DetectedObject> objectsAtTime = new ArrayList<>();
        objectsAtTime.add(new DetectedObject("Object1", "Description1"));
        objectsAtTime.add(new DetectedObject("Object2", "Description2"));
        StampedDetectedObjects stampedObjects = new StampedDetectedObjects(10, objectsAtTime);
        camera.addDetectedObject(stampedObjects);

        List<DetectedObject> retrievedObjects = camera.getDetectedObjectsAtTime(10);
        assertNotNull(retrievedObjects, "Objects at time 10 should not be null.");
        assertEquals(2, retrievedObjects.size(), "Two objects should be detected at time 10.");
        assertEquals("Object1", retrievedObjects.get(0).getId(), "First object ID should match.");
    }

    @Test
    public void testCheckForError() {
        // @PRE-CONDITION: Camera has detected objects, one of which is an error object.
        // @POST-CONDITION: An error description is returned.

        List<DetectedObject> objectsAtTime = new ArrayList<>();
        objectsAtTime.add(new DetectedObject("ERROR", "Camera disconnected"));
        StampedDetectedObjects errorObject = new StampedDetectedObjects(15, objectsAtTime);
        camera.addDetectedObject(errorObject);

        String errorDescription = camera.checkForError(15);
        assertNotNull(errorDescription, "Error description should not be null.");
        assertEquals("Camera disconnected", errorDescription, "Error description should match.");
    }
}