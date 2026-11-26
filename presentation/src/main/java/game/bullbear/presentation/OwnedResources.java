package game.bullbear.presentation;

import java.util.ArrayDeque;
import java.util.Deque;

final class OwnedResources {
    private final Deque<Runnable> release = new ArrayDeque<>();

    void add(Runnable cleanup) {
        release.push(cleanup);
    }

    void close() {
        Throwable failure = null;
        while (!release.isEmpty()) {
            try {
                release.pop().run();
            } catch (RuntimeException | Error problem) {
                if (failure == null) {
                    failure = problem;
                } else if (failure != problem) {
                    failure.addSuppressed(problem);
                }
            }
        }
        if (failure instanceof RuntimeException runtime) {
            throw runtime;
        }
        if (failure instanceof Error error) {
            throw error;
        }
    }
}
