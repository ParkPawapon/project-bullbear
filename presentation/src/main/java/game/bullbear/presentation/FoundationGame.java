package game.bullbear.presentation;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import game.bullbear.application.catalog.GameDesignCatalog;
import game.bullbear.presentation.screen.FoundationScreen;
import game.bullbear.presentation.theme.FoundationSkin;
import java.util.Objects;

public final class FoundationGame extends Game {
    private final GameDesignCatalog catalog;
    private final boolean smokeTest;
    private Skin skin;
    private boolean smokeFrameRendered;

    public FoundationGame(GameDesignCatalog catalog, boolean smokeTest) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.smokeTest = smokeTest;
    }

    @Override
    public void create() {
        try {
            skin = FoundationSkin.create();
            setScreen(new FoundationScreen(catalog, skin));
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
        var ownedScreen = getScreen();
        var ownedSkin = skin;
        screen = null;
        skin = null;
        try {
            if (ownedScreen != null) {
                ownedScreen.dispose();
            }
        } finally {
            if (ownedSkin != null) {
                ownedSkin.dispose();
            }
        }
    }
}
