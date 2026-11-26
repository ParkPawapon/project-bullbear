package game.bullbear.domain.game;

import java.util.Objects;

public record RoundKey(RunId runId, int stage, int round) {
    public static final int CAMPAIGN_STAGES = 7;
    public static final int ROUNDS_PER_STAGE = 3;

    public RoundKey {
        Objects.requireNonNull(runId);
        if (stage < 1 || round < 1 || round > ROUNDS_PER_STAGE) {
            throw new IllegalArgumentException("Stage or round is outside the valid range");
        }
    }

    public boolean endless() {
        return stage > CAMPAIGN_STAGES;
    }

    boolean finalRound() {
        return round == ROUNDS_PER_STAGE;
    }

    RoundKey nextRound() {
        return new RoundKey(runId, stage, Math.incrementExact(round));
    }

    RoundKey nextStage() {
        return new RoundKey(runId, Math.incrementExact(stage), 1);
    }
}
