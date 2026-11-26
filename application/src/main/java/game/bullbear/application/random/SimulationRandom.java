package game.bullbear.application.random;

import java.util.Random;
import java.util.random.RandomGenerator;

public final class SimulationRandom {
    private SimulationRandom() {}

    /** The JDK specifies Random's algorithm for portability across Java implementations. */
    public static RandomGenerator seeded(long seed) {
        return new Random(seed);
    }
}
