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
    private final ShellStrings strings = new ShellStrings(Locale.ENGLISH);
    private final OwnedResources resources = new OwnedResources();
    private boolean smokeFrameRendered;

    public FoundationGame(GameDesignCatalog catalog, boolean smokeTest) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.smokeTest = smokeTest;
    }

    @Override
    public void create() {
        try {
            var skin = FoundationSkin.create();
            resources.add(skin::dispose);
            var workspace = new FoundationScreen(catalog, skin, strings);
            resources.add(workspace::dispose);
            setScreen(workspace);
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
}
