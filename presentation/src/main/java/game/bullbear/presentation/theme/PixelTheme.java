package game.bullbear.presentation.theme;

import com.badlogic.gdx.graphics.Color;

public final class PixelTheme {
    public static final int CANVAS_WIDTH = 1280;
    public static final int CANVAS_HEIGHT = 720;
    public static final int SPACE_SMALL = 8;
    public static final int SPACE = 16;
    public static final int SPACE_LARGE = 32;

    static final int BACKGROUND = 0x102946FF;
    static final int PANEL = 0x151529FF;
    static final int SURFACE = 0x262640FF;
    static final int SURFACE_HOVER = 0x343454FF;
    static final int TASKBAR = 0x1A1A2AFF;
    static final int BORDER = 0x464657FF;
    static final int TEXT = 0xE4E4EFFF;
    static final int MUTED = 0xB4B4C6FF;
    static final int ACCENT = 0x4DA4DDFF;
    static final int WARNING = 0xF1CA49FF;

    private PixelTheme() {}

    public static Color background() {
        return new Color(BACKGROUND);
    }
}
