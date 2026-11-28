package game.bullbear.application.market;

import game.bullbear.application.random.SimulationRandom;
import game.bullbear.domain.market.MarketDraws;
import game.bullbear.domain.market.MarketRules;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/** Draw addresses are independent of invocation order, player actions and run identity. */
public final class MarketRandom implements MarketDraws {
    private static final String SCHEMA = "bullbear-market-rng-v1";
    private final byte[] prefix;

    public MarketRandom(long rootSeed, MarketRules rules) {
        byte[] schema = SCHEMA.getBytes(StandardCharsets.UTF_8);
        byte[] fingerprint = HexFormat.of().parseHex(rules.fingerprint());
        prefix =
                ByteBuffer.allocate(Integer.BYTES + schema.length + Long.BYTES + fingerprint.length)
                        .putInt(schema.length)
                        .put(schema)
                        .putLong(rootSeed)
                        .put(fingerprint)
                        .array();
    }

    @Override
    public int draw(long ordinal, Channel channel, String subject, int drawIndex, int bound) {
        if (ordinal <= 0 || drawIndex < 0 || bound <= 0) {
            throw new IllegalArgumentException("Invalid market draw address or bound");
        }
        byte[] channelBytes = channel.name().getBytes(StandardCharsets.UTF_8);
        byte[] subjectBytes = Objects.requireNonNull(subject).getBytes(StandardCharsets.UTF_8);
        byte[] address =
                ByteBuffer.allocate(
                                Long.BYTES
                                        + 3 * Integer.BYTES
                                        + channelBytes.length
                                        + subjectBytes.length)
                        .putLong(ordinal)
                        .putInt(channelBytes.length)
                        .put(channelBytes)
                        .putInt(subjectBytes.length)
                        .put(subjectBytes)
                        .putInt(drawIndex)
                        .array();
        MessageDigest digest = sha256();
        digest.update(prefix);
        long seed = ByteBuffer.wrap(digest.digest(address)).getLong();
        return SimulationRandom.seeded(seed).nextInt(bound);
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("JDK is missing SHA-256", e);
        }
    }
}
