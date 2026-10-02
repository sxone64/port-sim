package portsim.simulation.thread;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ShipThread implements Runnable {
    public enum Goal {
        ENTER_AND_DOCK,
        UNDOCK_AND_EXIT
    }

    @Override
    public void run() {
        // TODO
    }
}
