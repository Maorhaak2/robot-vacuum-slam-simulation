package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    private final int tickTime;
    private final int duration;
    private int currentTick = 0;
    private volatile boolean running = true;
    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        tickTime = TickTime;
        duration = Duration;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " started");
        this.subscribeBroadcast(TerminatedBroadcast.class, terminated -> {
            if(terminated.getMsName() == "FusionSlamService") {
                running = false;
            }
        });
        Thread timerThread = new Thread(() -> {
            try {
                while (currentTick < duration && running) {
                    // Increment tick count
                    currentTick++;
                    System.out.println("TimeService: Tick " + currentTick);
                    
                    // Send TickBroadcast
                    sendBroadcast(new TickBroadcast(currentTick));

                    // Sleep for tickTime duration
                    Thread.sleep(tickTime);
                }
                
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            } catch (InterruptedException e) {
                System.out.println("TimeService interrupted: " + e.getMessage());
            }
        });

        timerThread.start(); // Start the timer thread
    }
}
