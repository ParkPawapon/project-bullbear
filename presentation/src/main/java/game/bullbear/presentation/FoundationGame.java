package game.bullbear.presentation;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import game.bullbear.application.catalog.GameDesignCatalog;
import game.bullbear.presentation.screen.FoundationScreen;
import game.bullbear.presentation.theme.FoundationSkin;
import java.util.Locale;
import java.util.Objects;

public final class FoundationGame extends Game {
    private final GameDesignCatalog catalog;
    private final boolean smokeTest;
    private final boolean diagnostics;
    private final boolean thaiPreview;
    private final ShellStrings strings;
    private final OwnedResources resources = new OwnedResources();
    private boolean smokeFrameRendered;
    private long lastSample;

    public FoundationGame(GameDesignCatalog catalog, boolean smokeTest) {
        this(catalog, smokeTest, false, false);
    }

    public FoundationGame(
            GameDesignCatalog catalog,
            boolean smokeTest,
            boolean diagnostics,
            boolean thaiPreview) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.smokeTest = smokeTest;
        this.diagnostics = diagnostics;
        this.thaiPreview = thaiPreview;
        strings = new ShellStrings(Locale.ENGLISH);
    }

    @Override
    public void create() {
        try {
            if (thaiPreview) {
                var preview = new ThaiFontPreview();
                resources.add(preview::dispose);
                setScreen(preview);
                return;
            }
            var skin = FoundationSkin.create();
            resources.add(skin::dispose);
            var workspace = new FoundationScreen(catalog, skin, strings);
            resources.add(workspace::dispose);
            setScreen(workspace);
            if (diagnostics) {
                sample();
            }
        } catch (RuntimeException | Error failure) {
            try {
                dispose();
            } catch (RuntimeException | Error cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    @Override
    public void render() {
        super.render();
        if (diagnostics && System.nanoTime() - lastSample >= 1_000_000_000L) {
            sample();
        }
        if (smokeTest && !smokeFrameRendered) {
            smokeFrameRendered = true;
            Gdx.app.exit();
        }
    }

    public boolean smokeFrameRendered() {
        return smokeFrameRendered;
    }

    @Override
    public void dispose() {
        screen = null;
        resources.close();
    }

    private void sample() {
        lastSample = System.nanoTime();
        Gdx.app.log(
                "Bullbear diagnostics",
                String.format(
                        Locale.ROOT,
                        "fps=%d frameDeltaMs=%.2f logical=%dx%d framebuffer=%dx%d",
                        Gdx.graphics.getFramesPerSecond(),
                        Gdx.graphics.getDeltaTime() * 1000.0,
                        Gdx.graphics.getWidth(),
                        Gdx.graphics.getHeight(),
                        Gdx.graphics.getBackBufferWidth(),
                        Gdx.graphics.getBackBufferHeight()));
    }
}
