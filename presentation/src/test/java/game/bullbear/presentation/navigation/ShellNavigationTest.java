package game.bullbear.presentation.navigation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ShellNavigationTest {
    private final ShellNavigation navigation = new ShellNavigation();

    @Test
    void startsAtHome() {
        assertEquals(Route.HOME, navigation.current());
    }

    @ParameterizedTest
    @EnumSource(Route.class)
    void canNavigateToEveryRoute(Route route) {
        navigation.navigateTo(Route.BANK);
        navigation.navigateTo(route);
        assertEquals(route, navigation.current());
    }

    @ParameterizedTest
    @EnumSource(value = Route.class, names = "HOME", mode = EnumSource.Mode.EXCLUDE)
    void backReturnsHomeFromEachPlaceholder(Route route) {
        navigation.navigateTo(route);
        navigation.backToHome();
        assertEquals(Route.HOME, navigation.current());
    }

    @Test
    void repeatedBackAtHomeKeepsHome() {
        navigation.backToHome();
        navigation.backToHome();
        assertEquals(Route.HOME, navigation.current());
    }

    @Test
    void repeatedNavigationKeepsTheSelectedRoute() {
        navigation.navigateTo(Route.STOCKS);
        navigation.navigateTo(Route.STOCKS);
        assertEquals(Route.STOCKS, navigation.current());
    }

    @Test
    void rejectsNullWithoutChangingTheRoute() {
        navigation.navigateTo(Route.SHOP);
        assertThrows(NullPointerException.class, () -> navigation.navigateTo(null));
        assertEquals(Route.SHOP, navigation.current());
    }

    @Test
    void independentInstancesKeepIndependentRoutes() {
        var second = new ShellNavigation();
        navigation.navigateTo(Route.NEWS);
        second.navigateTo(Route.BANK);
        assertEquals(Route.NEWS, navigation.current());
        assertEquals(Route.BANK, second.current());
    }
}
