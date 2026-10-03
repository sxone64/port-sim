package portsim.simulation.session;

import org.jspecify.annotations.NullMarked;
import portsim.model.Terminal;
import portsim.simulation.navigation.TerminalNavigator;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@NullMarked
public final class TerminalSession {
    private final ReentrantLock lock;

    private final Terminal terminal;
    private final TerminalNavigator navigator;

    public TerminalSession(Terminal terminal) {
        this.terminal = terminal;
        this.lock = new ReentrantLock();
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

    public void locked(Runnable action) {
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }
}
