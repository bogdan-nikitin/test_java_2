package scene;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SceneTest {
    private static final List<String> PERSON = List.of(
            "Chandler", "Joey", "Monica", "Phoebe", "Rachel", "Ross"
    );
    Random random = new Random();

    void test(final List<String> input) {
        final String joined = String.join(System.lineSeparator(), input);
        final byte[] in = joined.getBytes(StandardCharsets.UTF_8);
        final Scene scene = new Scene();
        try {
            scene.read(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(in))));
            final ByteArrayOutputStream out = new ByteArrayOutputStream(in.length);
            final OutputStreamWriter writer = new OutputStreamWriter(out);
            scene.write(writer);
            writer.flush();
            assertEquals(joined, out.toString(StandardCharsets.UTF_8).strip());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    String randomText() {
        return String.valueOf(random.nextInt());
    }

    Stream<String> generateInput(final int size) {
        return Stream.generate(() -> STR."\{PERSON.get(random.nextInt(PERSON.size()))}: \{randomText()}").limit(size);
    }

    void testInvalid(final String invalidLine) {
        assertThrows(IllegalArgumentException.class, () -> test(Stream.of(generateInput(5),
                Stream.of(invalidLine),
                generateInput(5)).reduce(Stream::concat).get().toList()));
    }

    @Test
    @DisplayName("One line doesn't have a person")
    void test_noPerson() {
        testInvalid(randomText());
    }

    @Test
    @DisplayName("One line doesn't have space after :")
    void test_noSpace() {
        testInvalid(STR."\{PERSON.getFirst()}:\{randomText()}");
    }

    @Test
    @DisplayName("One line has unknown person")
    void test_wrongPerson() {
        testInvalid(STR."Elon: \{randomText()}");
    }

    @Test
    @DisplayName("Valid script")
    void test_valid() {
        IntStream.range(0, 1000).forEach(ignored -> test(generateInput(1000).toList()));
    }
}