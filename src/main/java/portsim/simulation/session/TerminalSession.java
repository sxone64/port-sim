package portsim.simulation.session;

import org.jspecify.annotations.NullMarked;
import portsim.model.Terminal;
import portsim.simulation.navigation.TerminalNavigator;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@NullMarked
public final class TerminalSession {
    private final ReentrantLock lock;
    private final Condition condition;

    private final Terminal terminal;
    private final TerminalNavigator navigator;

    public TerminalSession(Terminal terminal) {
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
        this.terminal = terminal;
        this.navigator = new TerminalNavigator(terminal);
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public TerminalNavigator getNavigator() {
        return navigator;
    }

    public <T> T lockedGet(Supplier<T> action) {
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    // Calling thread sleeps until either it's waken up by another thread or the specified time elapses
    public void waitSession(long millis) throws InterruptedException {
        lock.lock();
        try {
            var _ = condition.await(millis, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public void signalAll() {
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
