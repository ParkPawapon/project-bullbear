package game.bullbear.presentation.navigation;

public enum Route {
    HOME("Home"),
    NEWS("News"),
    STOCKS("Stocks"),
    SHOP("Shop"),
    BANK("Bank");

    private final String title;

    Route(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
