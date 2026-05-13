package optimised;

import java.util.ArrayList;
import java.util.List;


public class NotificationService {
    private final List<String> log = new ArrayList<>();

    public void notify(String recipient, Submission s, Outcome outcome) {
        InteractionCounter.tick();
        log.add(outcome.name() + ":" + s.getId() + "->" + recipient);
    }

    public List<String> getLog() { return log; }
}
