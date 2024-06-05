package scene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.FormatProcessor.FMT;

public class Scene {
    private static final Set<String> PERSON = Set.of(
            "Chandler", "Joey", "Monica", "Phoebe", "Rachel", "Ross"
    );

    private final Map<String, Phaser> personBarriers;
    private final Map<String, List<Phrase>> personSpeech;
    private String firstPerson = null;

    public Scene() {
        personBarriers = PERSON.stream().collect(Collectors.toMap(Function.identity(), ignored -> new Phaser(2)));
        personSpeech = PERSON.stream().collect(Collectors.toMap(Function.identity(), ignored -> new ArrayList<>()));
    }

    public void read(final BufferedReader reader) throws IOException {
        record Line(String person, String text) {}
        List<Line> script = reader.lines().map(line -> {
            final int index = line.indexOf(":");
            if (index == -1) {
                throw new IllegalArgumentException("Invalid input format");
            }
            final String person = line.substring(0, index);
            if (!PERSON.contains(person)) {
                throw new IllegalArgumentException(STR."Invalid name: \{person}");
            }
            return new Line(person, line.substring(index + 1));
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

    public void write(final OutputStreamWriter writer) throws IOException {
        try (ExecutorService executor = Executors.newFixedThreadPool(PERSON.size())) {
            PERSON.forEach(person -> executor.submit(() -> {
                                for (final Phrase phrase : personSpeech.get(person)) {
                                    personBarriers.get(person).arriveAndAwaitAdvance();
                                    writer.write(FMT."\{person}:\{phrase.text()}\n");
                                    if (phrase.nextPerson() != null) {
                                        personBarriers.get(phrase.nextPerson()).arrive();
                                    }
                                }
                                return null;
                            }
                    )
            );
            if (firstPerson != null) {
                personBarriers.get(firstPerson).arrive();
            }
        }
    }

    private record Phrase(String text, String nextPerson) {
    }
}
