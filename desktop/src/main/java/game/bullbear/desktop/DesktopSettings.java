package game.bullbear.desktop;

import game.bullbear.presentation.theme.PixelTheme;
import java.util.Locale;
import java.util.Objects;

record DesktopSettings(
        int width, int height, Locale locale, boolean audio, float volume, boolean diagnostics) {
    static final int MIN_WIDTH = 800;
    static final int MIN_HEIGHT = 450;
    static final Locale THAI = Locale.forLanguageTag("th");
    static final DesktopSettings DEFAULT =
            new DesktopSettings(
                    PixelTheme.CANVAS_WIDTH,
                    PixelTheme.CANVAS_HEIGHT,
                    Locale.ENGLISH,
                    false,
                    0.75f,
                    false);

    DesktopSettings {
        Objects.requireNonNull(locale, "locale");
        if (width < MIN_WIDTH || width > 7680 || height < MIN_HEIGHT || height > 4320) {
            throw new IllegalArgumentException("Window dimensions out of range");
        }
        if (!locale.equals(Locale.ENGLISH) && !locale.equals(THAI)) {
            throw new IllegalArgumentException("Unsupported locale");
        }
        if (!Float.isFinite(volume) || volume < 0 || volume > 1) {
            throw new IllegalArgumentException("Volume out of range");
        }
    }
}
