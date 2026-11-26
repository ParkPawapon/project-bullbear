package game.bullbear.presentation;

import game.bullbear.presentation.navigation.Route;
import java.util.Locale;
import java.util.ResourceBundle;

public final class ShellStrings {
    static final String BUNDLE = "game.bullbear.presentation.messages";
    private final ResourceBundle bundle;

    public ShellStrings(Locale locale) {
        bundle =
                ResourceBundle.getBundle(
                        BUNDLE,
                        locale,
                        ResourceBundle.Control.getNoFallbackControl(
                                ResourceBundle.Control.FORMAT_PROPERTIES));
    }

    public String get(String key) {
        return bundle.getString(key);
    }

    public String route(Route route) {
        return get("route." + route.name().toLowerCase(Locale.ROOT));
    }

    public String catalog(int stocks, int sources, int charms) {
        return String.format(bundle.getLocale(), get("catalog"), stocks, sources, charms);
    }
}
