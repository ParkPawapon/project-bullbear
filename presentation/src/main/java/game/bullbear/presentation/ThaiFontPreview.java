package game.bullbear.presentation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import game.bullbear.presentation.theme.PixelTheme;

/** Technical preview of rasterized Thai examples from the HarfBuzz shaping pipeline. */
final class ThaiFontPreview extends ScreenAdapter {
    private final OwnedResources resources = new OwnedResources();
    private final Color background = PixelTheme.background();
    private final Stage stage;
    private final InputAdapter input =
            new InputAdapter() {
                @Override
                public boolean keyUp(int keycode) {
                    if (keycode == Keys.ESCAPE) {
                        Gdx.app.exit();
                        return true;
                    }
                    return false;
                }
            };

    ThaiFontPreview() {
        try {
            var atlas = new Texture(Gdx.files.classpath("assets/thai-preview.png"));
            resources.add(atlas::dispose);
            atlas.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            stage = new Stage(new FitViewport(PixelTheme.CANVAS_WIDTH, PixelTheme.CANVAS_HEIGHT));
            resources.add(stage::dispose);
            var layout = new Table();
            layout.setFillParent(true);
            layout.add(new Image(atlas)).size(960, 570);
            stage.addActor(layout);
        } catch (RuntimeException | Error failure) {
            try {
                resources.close();
            } catch (RuntimeException | Error cleanup) {
                failure.addSuppressed(cleanup);
            }
            throw failure;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(input);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(background);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width > 0 && height > 0) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == input) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        try {
            hide();
        } finally {
            resources.close();
        }
    }
}
