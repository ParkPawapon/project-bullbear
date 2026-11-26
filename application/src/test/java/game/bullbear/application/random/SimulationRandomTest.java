package game.bullbear.application.random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SimulationRandomTest {
    @Test
    void seededInstancesAreRepeatableAndIndependent() {
        var first = SimulationRandom.seeded(281);
        var second = SimulationRandom.seeded(281);
        for (int count = 0; count < 15; count++) {
            int value = first.nextInt(40);
            assertTrue(value >= 0 && value < 40);
            assertEquals(value, second.nextInt(40));
        }
        assertThrows(IllegalArgumentException.class, () -> second.nextInt(0));
    }
}
