package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Pose;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

class MessageBusImplTest {

    private MessageBusImpl testMessageBus;
    private MicroService serviceA;
    private MicroService serviceB;

    @BeforeEach
    public void setUp() {
        // Set up the MessageBus instance and MicroServices for testing
        testMessageBus = MessageBusImpl.getInstance();
        serviceA = new MockMicroService("ServiceA");
        serviceB = new MockMicroService("ServiceB");
    }

    @Test
    public void testRegisterAndUnregisterService() {
        // ** Test registering a MicroService **
        testMessageBus.register(serviceA);
        assertTrue(testMessageBus.getMicroServiceQueues().containsKey(serviceA), 
                "ServiceA should be registered and have a queue.");

        // ** Test unregistering the MicroService **
        testMessageBus.unregister(serviceA);
        assertFalse(testMessageBus.getMicroServiceQueues().containsKey(serviceA), 
                "ServiceA should be unregistered and its queue removed.");
    }

    @Test
    public void testEventSubscription() {
        // ** Test subscribing a MicroService to an event **
        testMessageBus.register(serviceA);
        testMessageBus.subscribeEvent(PoseEvent.class, serviceA);
        assertTrue(testMessageBus.getEventSubscribers().get(PoseEvent.class).contains(serviceA),
                "ServiceA should be subscribed to PoseEvent.");
    }

    @Test
    public void testBroadcastSubscriptionAndDelivery() throws InterruptedException {
        // ** Test subscribing MicroServices to a broadcast and delivering it **
        testMessageBus.register(serviceA);
        testMessageBus.register(serviceB);
        testMessageBus.subscribeBroadcast(TickBroadcast.class, serviceA);
        testMessageBus.subscribeBroadcast(TickBroadcast.class, serviceB);

        TickBroadcast testBroadcast = new TickBroadcast(0);
        testMessageBus.sendBroadcast(testBroadcast);

        BlockingQueue<Message> queueA = testMessageBus.getMicroServiceQueues().get(serviceA);
        BlockingQueue<Message> queueB = testMessageBus.getMicroServiceQueues().get(serviceB);

        assertEquals(testBroadcast, queueA.poll(100, TimeUnit.MILLISECONDS), 
                "ServiceA should have received the TickBroadcast.");
        assertEquals(testBroadcast, queueB.poll(100, TimeUnit.MILLISECONDS), 
                "ServiceB should have received the TickBroadcast.");
    }

    @Test
    public void testEventDeliveryAndCompletion() throws InterruptedException {
        // ** Test sending an event and completing it **
        testMessageBus.register(serviceA);
        testMessageBus.subscribeEvent(PoseEvent.class, serviceA);

        Pose pose = new Pose(7, 3, 30, 8);
        PoseEvent testEvent = new PoseEvent(pose);

        Future<Pose> testFuture = (Future<Pose>) testMessageBus.sendEvent(testEvent);
        assertNotNull(testFuture, "Future should be returned for the event.");

        testMessageBus.complete(testEvent, pose);
        assertEquals(pose, testFuture.get(), "The event result should match the completed event.");
    }

    @Test
    public void testAwaitMessageWithBroadcast() throws InterruptedException {
        // ** Test awaiting a message (broadcast) **
        testMessageBus.register(serviceA);
        testMessageBus.subscribeBroadcast(TickBroadcast.class, serviceA);

        TickBroadcast broadcastMessage = new TickBroadcast(0);
        testMessageBus.sendBroadcast(broadcastMessage);

        Message receivedMessage = testMessageBus.awaitMessage(serviceA);
        assertEquals(broadcastMessage, receivedMessage, "The awaited message should be the TickBroadcast.");
    }

    @Test
    public void testInvalidAwaitMessage() {
        // ** Test awaiting a message for an unregistered MicroService **
        assertThrows(IllegalStateException.class, () -> testMessageBus.awaitMessage(serviceA),
                "Awaiting message for an unregistered MicroService should throw an exception.");
    }

    @Test
    public void testMultipleEventSubscriptions() {
        // ** Test subscribing multiple MicroServices to the same event **
        testMessageBus.register(serviceA);
        testMessageBus.register(serviceB);

        testMessageBus.subscribeEvent(PoseEvent.class, serviceA);
        testMessageBus.subscribeEvent(PoseEvent.class, serviceB);

        assertTrue(testMessageBus.getEventSubscribers().get(PoseEvent.class).contains(serviceA),
                "ServiceA should be subscribed to PoseEvent.");
        assertTrue(testMessageBus.getEventSubscribers().get(PoseEvent.class).contains(serviceB),
                "ServiceB should be subscribed to PoseEvent.");
    }

    // Mock MicroService class for testing
    private class MockMicroService extends MicroService {
        public MockMicroService(String name) {
            super(name);
        }

        @Override
        protected void initialize() {
            // No initialization needed for testing
        }
    }
}