package game.bullbear.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class OwnedResourcesTest {
    @Test
    void releasesBorrowersBeforeTheirSharedAssetsOnce() {
        var released = new ArrayList<String>();
        var owner = new OwnedResources();
        owner.add(() -> released.add("skin"));
        owner.add(() -> released.add("stage"));
        owner.close();
        owner.close();
        assertEquals(List.of("stage", "skin"), released);
    }

    @Test
    void releasesRemainingResourcesAndPreservesBothFailures() {
        var owner = new OwnedResources();
        var primary = new IllegalStateException("stage");
        var secondary = new IllegalStateException("skin");
        owner.add(
                () -> {
                    throw secondary;
                });
        owner.add(
                () -> {
                    throw primary;
                });
        assertSame(primary, assertThrows(IllegalStateException.class, owner::close));
        assertSame(secondary, primary.getSuppressed()[0]);
        owner.close();
    }
}
