package game.bullbear.domain.game;

public enum GamePhase {
    ROUND_ENTRY,
    PLANNING,
    INVESTMENT_COMMITTED,
    POST_RESULT,
    VICTORY,
    GAME_OVER,
    FINISHED;

    public boolean terminal() {
        return this == GAME_OVER || this == FINISHED;
    }

    public boolean stableInteraction() {
        return this == PLANNING || this == POST_RESULT;
    }
}
