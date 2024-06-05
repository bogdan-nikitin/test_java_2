package scene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Main {
    public static void main(String[] args) {
        final Scene scene = new Scene();
        try {
            scene.read(new BufferedReader(new InputStreamReader(System.in)));
            final OutputStreamWriter writer = new OutputStreamWriter(System.out);
            scene.write(writer);
            writer.flush();
        } catch (final IOException e) {
        }
    }
}