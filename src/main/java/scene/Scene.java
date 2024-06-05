package scene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Phaser;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.FormatProcessor.FMT;

/**
 * Scene with 6 characters: Chandler, Joey, Monica, Phoebe, Rachel and Ross.
 * Each character has its lines and writes them in separate thread.
 *
 * Script has the following format:
 * <pre>
 * character1: line1
 * character2: line2
 * characterN: lineN
 * </pre>
 *
 * Methods of this class cannot be called simultaneously.
 *
 * @author Bogdan Nikitin
 */
public class Scene {
    private static final Set<String> PERSON = Set.of(
            "Chandler", "Joey", "Monica", "Phoebe", "Rachel", "Ross"
    );

    private final Map<String, Phaser> personBarriers;
    private final Map<String, List<Phrase>> personSpeech;
    private String firstPerson = null;

    /**
     * Constructs empty scene.
     */
    public Scene() {
        personBarriers = PERSON.stream().collect(Collectors.toMap(Function.identity(), ignored -> new Phaser(2)));
        personSpeech = PERSON.stream().collect(Collectors.toMap(Function.identity(), ignored -> new ArrayList<>()));
    }

    /**
     * Reads script from {@code BufferedReader}.
     * Script must have the following format:
     * <pre>
     * character1: line1
     * character2: line2
     * characterN: lineN
     * </pre>
     * @param reader reader to read script from.
     * @throws IOException if IO error occurs.
     * @throws IllegalArgumentException if script has invalid format.
     */
    public void read(final BufferedReader reader) throws IOException {
        record Line(String person, String text) {
        }
        List<Line> script = reader.lines().map(line -> {
            final int index = line.indexOf(": ");
            if (index == -1) {
                throw new IllegalArgumentException("Invalid input format");
            }
            final String person = line.substring(0, index);
            if (!PERSON.contains(person)) {
                throw new IllegalArgumentException(STR."Invalid name: \{person}");
            }
            return new Line(person, line.substring(index + 2));
        }).toList();
        if (!script.isEmpty()) {
            firstPerson = script.getFirst().person();
        }
        for (int i = 0; i < script.size(); ++i) {
            Line line = script.get(i);
            personSpeech.get(line.person()).add(
                    new Phrase(line.text(), i + 1 < script.size() ? script.get(i + 1).person() : null));
        }
    }

    /**
     * Writes script to provided {@code OutputStreamWriter}.
     * Each character write its line in separate thread.
     * @param writer writer to write script to.
     * @throws IOException if IO error occurs.
     */
    public void write(final OutputStreamWriter writer) throws IOException {
        final List<Thread> threads;
        try {
            threads = PERSON.stream().map(person -> new Thread(() -> {
                for (final Phrase phrase : personSpeech.get(person)) {
                    personBarriers.get(person).arriveAndAwaitAdvance();
                    try {
                        writer.write(FMT."\{person}: \{phrase.text()}\n");
                    } catch (final IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    if (phrase.nextPerson() != null) {
                        personBarriers.get(phrase.nextPerson()).arrive();
                    }
                }
            })).toList();
        } catch (final UncheckedIOException e) {
            throw e.getCause();
        }
        threads.forEach(Thread::start);
        if (firstPerson != null) {
            personBarriers.get(firstPerson).arrive();
        }
        boolean interrupted = Thread.interrupted();
        for (int i = 0; i < threads.size(); ) {
            try {
                threads.get(i).join();
                ++i;
            } catch (final InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private record Phrase(String text, String nextPerson) {
    }
}
