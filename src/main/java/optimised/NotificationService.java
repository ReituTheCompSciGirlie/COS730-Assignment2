package optimised;

import java.util.ArrayList;
import java.util.List;

/**
 * Single notification entry-point. Replaces the three baseline
 * notify* methods with one polymorphic notify(recipient, outcome).
 * Adding a new Outcome variant requires zero changes here.
 */
public class NotificationService {
    private final List<String> log = new ArrayList<>();

    public void notify(String recipient, Submission s, Outcome outcome) {
        InteractionCounter.tick();
        log.add(outcome.name() + ":" + s.getId() + "->" + recipient);
    }

    public List<String> getLog() { return log; }
}
