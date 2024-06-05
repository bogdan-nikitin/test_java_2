package scene;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Scene {
    private static final Set<String> PERSON = Set.of(
            "Chandler", "Joey", "Monica", "Phoebe", "Rachel", "Ross"
    );
    private record Phrase(String text, String previousPerson) {}
    private final Map<String, List<Phrase>> speech = Map.of(
            "Chandler", new ArrayList<>(),
            "Joey", new ArrayList<>(),
            "Monica", new ArrayList<>(),
            "Phoebe", new ArrayList<>(),
            "Rachel", new ArrayList<>(),
            "Ross", new ArrayList<>()
    );

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
            speech.get(person).add(new Phrase(line.substring(index + 1), previousPerson));
            previousPerson = person;
        }
    }
}
