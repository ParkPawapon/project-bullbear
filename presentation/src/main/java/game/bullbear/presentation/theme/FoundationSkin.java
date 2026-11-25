package game.bullbear.presentation.theme;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public final class FoundationSkin {
    private FoundationSkin() {}

    public static Skin create() {
        var skin = new Skin();
        try {
            var pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            try {
                pixmap.setColor(Color.WHITE);
                pixmap.fill();
                var texture = new Texture(pixmap);
                skin.add("pixel", texture);
                skin.add("pixel", new TextureRegion(texture));
            } finally {
                pixmap.dispose();
            }

            addFill(skin, "panel", PixelTheme.PANEL);
            addFill(skin, "toolbar", PixelTheme.TASKBAR);
            addFill(skin, "border", PixelTheme.BORDER);
            addFill(skin, "accent", PixelTheme.ACCENT);
            addFill(skin, "muted", PixelTheme.MUTED);
            addFill(skin, "button", PixelTheme.SURFACE);
            addFill(skin, "hover", PixelTheme.SURFACE_HOVER);
            addFill(skin, "pressed", PixelTheme.BACKGROUND);

            var font = new BitmapFont();
            skin.add("font", font);
            font.getRegion()
                    .getTexture()
                    .setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            skin.add("default", new LabelStyle(font, new Color(PixelTheme.TEXT)));
            skin.add("muted", new LabelStyle(font, new Color(PixelTheme.MUTED)));
            skin.add("status", new LabelStyle(font, new Color(PixelTheme.WARNING)));

            var button =
                    new TextButtonStyle(
                            skin.getDrawable("button"), skin.getDrawable("pressed"), null, font);
            button.over = skin.getDrawable("hover");
            button.focused = skin.getDrawable("accent");
            button.checkedFocused = button.focused;
            button.focusedFontColor = new Color(PixelTheme.PANEL);
            button.checkedFocusedFontColor = button.focusedFontColor;
            button.fontColor = new Color(PixelTheme.TEXT);
            button.overFontColor = button.fontColor;
            button.downFontColor = button.fontColor;
            button.disabled = skin.getDrawable("toolbar");
            button.disabledFontColor = new Color(PixelTheme.MUTED);
            skin.add("default", button);
            return skin;
        } catch (RuntimeException | Error failure) {
            try {
                skin.dispose();
            } catch (RuntimeException | Error cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    private static void addFill(Skin skin, String name, int rgba) {
        skin.add(name, skin.newDrawable("pixel", new Color(rgba)), Drawable.class);
    }
}
