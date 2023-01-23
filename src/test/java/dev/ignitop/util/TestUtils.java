package dev.ignitop.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import dev.ignitop.ui.component.TerminalComponent;

/**
 *
 */
public class TestUtils {
    /**
     * @param component Component.
     * @param width Width.
     */
    public static String renderToString(TerminalComponent component, int width) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PrintStream out = new PrintStream(baos);

            component.render(width, out);

            out.flush();

            return baos.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
