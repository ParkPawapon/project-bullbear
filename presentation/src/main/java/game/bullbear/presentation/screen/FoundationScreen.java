package game.bullbear.presentation.screen;

import static game.bullbear.presentation.theme.PixelTheme.SPACE;
import static game.bullbear.presentation.theme.PixelTheme.SPACE_LARGE;
import static game.bullbear.presentation.theme.PixelTheme.SPACE_SMALL;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import game.bullbear.application.catalog.GameDesignCatalog;
import game.bullbear.presentation.ShellStrings;
import game.bullbear.presentation.navigation.Route;
import game.bullbear.presentation.navigation.ShellNavigation;
import game.bullbear.presentation.theme.PixelTheme;
import java.util.ArrayList;
import java.util.List;

public final class FoundationScreen extends ScreenAdapter {
    private static final List<Route> FEATURES =
            List.of(Route.NEWS, Route.STOCKS, Route.SHOP, Route.BANK);
    private static final int FRAME_WIDTH = 960;
    private static final int FRAME_HEIGHT = 420;
    private static final int CONTROL_HEIGHT = 60;

    private final Stage stage;
    private final Skin skin;
    private final ShellStrings strings;
    private final Color background = PixelTheme.background();
    private final ShellNavigation navigation = new ShellNavigation();
    private final Table content = new Table();
    private final Table controls = new Table();
    private final List<TextButton> focusOrder = new ArrayList<>();
    private boolean disposed;

    public FoundationScreen(GameDesignCatalog catalog, Skin skin, ShellStrings strings) {
        this.skin = skin;
        this.strings = strings;
        stage = new Stage(new FitViewport(PixelTheme.CANVAS_WIDTH, PixelTheme.CANVAS_HEIGHT));
        try {
            buildFrame(catalog);
            showRoute();
            bindKeyboard();
        } catch (RuntimeException | Error failure) {
            try {
                dispose();
            } catch (RuntimeException | Error cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    private void buildFrame(GameDesignCatalog catalog) {
        var workspace = new Table();
        workspace.setFillParent(true);
        stage.addActor(workspace);

        var frame = new Table();
        frame.setBackground(skin.getDrawable("border"));
        frame.pad(2);
        workspace.add(frame).size(FRAME_WIDTH, FRAME_HEIGHT);
        frame.add(new Image(skin.getDrawable("accent"))).height(4).growX().row();

        var header = new Table();
        header.setBackground(skin.getDrawable("toolbar"));
        header.pad(SPACE, SPACE_LARGE, SPACE, SPACE_LARGE);
        header.add(label(strings.get("app.title"), "default", 1.65f)).growX().left();
        header.add(label(strings.get("app.phase"), "status", 1.15f)).right();
        frame.add(header).growX().row();

        var panel = new Table();
        panel.setBackground(skin.getDrawable("panel"));
        panel.pad(SPACE_LARGE);
        panel.add(content).grow().row();
        panel.add(controls).growX();
        frame.add(panel).grow().row();

        var footer = new Table();
        footer.setBackground(skin.getDrawable("toolbar"));
        footer.pad(SPACE_SMALL, SPACE_LARGE, SPACE_SMALL, SPACE_LARGE);
        var metadata =
                strings.catalog(
                        catalog.stockIds().size(),
                        catalog.sourceIds().size(),
                        catalog.charmCategories().size());
        footer.add(label(metadata, "muted", 1.1f)).growX().left();
        frame.add(footer).growX();
    }

    private void showRoute() {
        stage.setKeyboardFocus(null);
        focusOrder.clear();
        content.clearChildren();
        controls.clearChildren();

        var route = navigation.current();
        var heading = label(strings.route(route), "default", 2.7f);
        heading.setAlignment(Align.center);
        content.add(heading).center();
        if (route != Route.HOME) {
            content.row();
            // The bundled bitmap font has no em dash glyph.
            var status = new Table();
            status.add(label(strings.get("status.unavailable"), "muted", 1.45f));
            status.add(new Image(skin.getDrawable("muted")))
                    .size(SPACE, 2)
                    .pad(0, SPACE_SMALL, 0, SPACE_SMALL);
            status.add(label(strings.get("status.phase"), "muted", 1.45f));
            content.add(status).padTop(SPACE_SMALL).top();
        }

        if (route == Route.HOME) {
            for (Route feature : FEATURES) {
                controls.add(
                                button(
                                        strings.route(feature),
                                        () -> {
                                            navigation.navigateTo(feature);
                                            showRoute();
                                        }))
                        .height(CONTROL_HEIGHT)
                        .uniformX()
                        .growX()
                        .spaceRight(SPACE);
            }
            controls.row();
        } else {
            controls.add(button(strings.get("action.back"), this::goHome))
                    .width(192)
                    .height(CONTROL_HEIGHT)
                    .left();
        }
        var exit =
                controls.add(button(strings.get("action.exit"), () -> Gdx.app.exit()))
                        .height(44)
                        .width(112)
                        .right();
        if (route == Route.HOME) {
            exit.colspan(FEATURES.size()).padTop(SPACE);
        } else {
            exit.expandX();
        }
    }

    private Label label(String text, String style, float scale) {
        var label = new Label(text, skin, style);
        label.setFontScale(scale);
        return label;
    }

    private TextButton button(String title, Runnable activate) {
        var button = new TextButton(title, skin);
        button.getLabel().setFontScale(1.45f);
        button.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        activate.run();
                    }
                });
        button.addListener(
                new InputListener() {
                    @Override
                    public boolean touchDown(
                            InputEvent event, float x, float y, int pointer, int mouseButton) {
                        if (pointer == 0 && mouseButton == Buttons.LEFT) {
                            stage.setKeyboardFocus(button);
                        }
                        return false;
                    }
                });
        focusOrder.add(button);
        return button;
    }

    private void bindKeyboard() {
        stage.addListener(
                new InputListener() {
                    private boolean leftShift;
                    private boolean rightShift;

                    @Override
                    public boolean keyDown(InputEvent event, int keycode) {
                        // The backend may queue a whole shortcut before it dispatches the events.
                        if (keycode == Keys.SHIFT_LEFT) {
                            leftShift = true;
                        } else if (keycode == Keys.SHIFT_RIGHT) {
                            rightShift = true;
                        }
                        if (keycode == Keys.TAB) {
                            boolean backwards = leftShift || rightShift;
                            int current = focusOrder.indexOf(stage.getKeyboardFocus());
                            int next =
                                    current < 0
                                            ? (backwards ? focusOrder.size() - 1 : 0)
                                            : Math.floorMod(
                                                    current + (backwards ? -1 : 1),
                                                    focusOrder.size());
                            stage.setKeyboardFocus(focusOrder.get(next));
                            return true;
                        }
                        if (keycode == Keys.ESCAPE && navigation.current() != Route.HOME) {
                            goHome();
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public boolean keyUp(InputEvent event, int keycode) {
                        if (keycode == Keys.SHIFT_LEFT) {
                            leftShift = false;
                        } else if (keycode == Keys.SHIFT_RIGHT) {
                            rightShift = false;
                        }
                        if ((keycode == Keys.ENTER || keycode == Keys.SPACE)
                                && stage.getKeyboardFocus() instanceof TextButton button
                                && !button.isDisabled()) {
                            button.fire(new ChangeListener.ChangeEvent());
                            return true;
                        }
                        return false;
                    }
                });
    }

    private void goHome() {
        navigation.backToHome();
        showRoute();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(background);
        stage.act(delta);
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
        if (Gdx.input.getInputProcessor() == stage) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            try {
                hide();
            } finally {
                stage.dispose();
            }
        }
    }
}
