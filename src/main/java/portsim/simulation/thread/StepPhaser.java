package portsim.simulation.thread;

import org.jspecify.annotations.NullMarked;

import java.time.Duration;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/*
    Tracks the acknowledgment for a single simulation step (one event, all listeners).

    A ship thread creates one instance per step and hands it to the navigator,
    which creates one acknowledgment per listener while firing the event.
    After the lock is released, the ship thread calls await,
    which returns once every listener has acknowledged the step (or the timeout elapses)
 */
@NullMarked
public final class StepPhaser {
    // First party is always the waiting ship thread, every acknowledgment registers one more party
    private final Phaser phaser = new Phaser(1);

    public Runnable createAck() {
        phaser.register();

        // Guard against multiple acknowledgment calls
        var fired = new AtomicBoolean();

        return () -> {
            if (fired.compareAndSet(false, true))
                phaser.arriveAndDeregister();
        };
    }

    /*
        Calling ship thread blocks until all parties arrive
        (every acknowledgment has been called) or timeout elapses
     */
    public boolean await(Duration timeout) throws InterruptedException {
        // Calling ship thread has arrived
        var phase = phaser.arrive();

        try {
            phaser.awaitAdvanceInterruptibly(phase, timeout.toMillis(), TimeUnit.MILLISECONDS);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}
