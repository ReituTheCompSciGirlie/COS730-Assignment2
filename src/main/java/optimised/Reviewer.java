package optimised;

import java.util.HashSet;
import java.util.Set;

/**
 * Reviewer in the optimised model. Crucially the Reviewer no longer
 * holds a Database reference and no longer persists its own scores -
 * that responsibility now belongs to EvaluationManager (Information
 * Expert: EM owns the EvaluationResult, EM persists it).
 */
public class Reviewer {
    private final String id, name;
    private final Set<String> conflictsWith = new HashSet<>();
    private int currentWorkload;

    public Reviewer(String id, String name, int currentWorkload) {
        this.id = id; this.name = name; this.currentWorkload = currentWorkload;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public int getCurrentWorkload() { return currentWorkload; }
    public Set<String> getConflictsWith() { return conflictsWith; }
    public void addConflict(String email) { conflictsWith.add(email); }
    public void incrementWorkload() { currentWorkload++; }

    /**
     * Reviewer just produces a score; persistence is handled by the
     * EvaluationManager. The Database reference has been removed.
     */
    public int score(Submission s) {
        InteractionCounter.tick();
        return Math.abs((s.getId() + this.id).hashCode()) % 11;
    }
}
