package game.bullbear.presentation.navigation;

import java.util.Objects;

public final class ShellNavigation {
    private Route current = Route.HOME;

    public Route current() {
        return current;
    }

    public void navigateTo(Route route) {
        current = Objects.requireNonNull(route, "route");
    }

    public void backToHome() {
        navigateTo(Route.HOME);
    }
}
