package game.bullbear.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import game.bullbear.presentation.navigation.Route;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class ShellStringsTest {
    @Test
    void loadsUtf8AndKeepsBothBundlesInSync() throws IOException {
        String base = "/" + ShellStrings.BUNDLE.replace('.', '/');
        assertEquals(
                keys(base + ".properties").stringPropertyNames(),
                keys(base + "_th.properties").stringPropertyNames());
        var thai = new ShellStrings(Locale.forLanguageTag("th"));
        assertEquals("ร้านค้า", thai.route(Route.SHOP));
        assertEquals(
                "หมวดข้อมูล: หุ้น 5 / แหล่งข่าว 5 / ประเภทเครื่องราง 3", thai.catalog(5, 5, 3));
    }

    @Test
    void unsupportedLocaleFallsBackToEnglishAndUnknownKeyFails() {
        var english = new ShellStrings(Locale.FRENCH);
        assertEquals("News", english.route(Route.NEWS));
        assertThrows(MissingResourceException.class, () -> english.get("missing.key"));
    }

    private Properties keys(String name) throws IOException {
        var stream = getClass().getResourceAsStream(name);
        assertNotNull(stream);
        var properties = new Properties();
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }
}
