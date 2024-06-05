package scene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Read scene from console.
 *
 * @author Bogdan Nikitin
 */
public class Main {
    /**
     * Construct {@code Main}.
     */
    public Main() {
    }

    /**
     * Read scene script from console and then print it.
     * Each character prints lines in separate thread.
     *
     * Reads until EOF. Input should not contain empty lines at the end.
     * @param args command line arguments, ignored.
     */
    public static void main(final String[] args) {
        final Scene scene = new Scene();
        try {
            scene.read(new BufferedReader(new InputStreamReader(System.in)));
            final OutputStreamWriter writer = new OutputStreamWriter(System.out);
            scene.write(writer);
            writer.flush();
        } catch (final IOException e) {
            System.err.println(STR."IO error occurred: \{e.getMessage()}");
        }
    }
}