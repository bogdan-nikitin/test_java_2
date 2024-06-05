package scene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Scene {
    private static final Set<String> PERSON = Set.of(
            "Chandler", "Joey", "Monica", "Phoebe", "Rachel", "Ross"
    );

    private final Map<String, Phaser> personBarriers;
    private final Map<String, List<Phrase>> personSpeech;


    private record Phrase(String text, String previousPerson) {}
    private String firstPerson;

    public Scene() {
        personBarriers = PERSON.stream().collect(Collectors.toMap(Function.identity(), ignored -> new Phaser(1)));
        personSpeech = PERSON.stream().collect(Collectors.toMap(Function.identity(), ignored -> new ArrayList<>()));
    }

    public void read(final BufferedReader reader) throws IOException {
        String previousPerson = null;
        while (true) {
            final String line = reader.readLine();
            if (line == null) {
                break;
            }
            final int index = line.indexOf(":");
            if (index == -1) {
                throw new IllegalArgumentException("Invalid input format");
            }
            final String person = line.substring(0, index);
            if (!PERSON.contains(person)) {
                throw new IllegalArgumentException(STR."Invalid name: \{person}");
            }
            personSpeech.get(person).add(new Phrase(line.substring(index + 1), previousPerson));
            previousPerson = person;
        }
    }

    public void write(final BufferedWriter writer) throws IOException {
        try (ExecutorService executor = Executors.newFixedThreadPool(PERSON.size())) {
        }
    }
}
