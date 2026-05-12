package baseline;

import java.util.HashSet;
import java.util.Set;

/**
 * Reviewer participant. In the baseline design the Reviewer talks
 * directly to the Database to save its own scores - this is one of the
 * deliberate violations of GRASP Information Expert that the assignment
 * asks us to identify.
 */
public class Reviewer {
    private final String id;
    private final String name;
    private final Set<String> conflictsWith = new HashSet<>();
    private int currentWorkload;
    private final Database db;          // tight coupling: reviewer knows DB

    public Reviewer(String id, String name, int currentWorkload, Database db) {
        this.id = id;
        this.name = name;
        this.currentWorkload = currentWorkload;
        this.db = db;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getCurrentWorkload() { return currentWorkload; }
    public Set<String> getConflictsWith() { return conflictsWith; }

    public void addConflict(String authorEmail) { conflictsWith.add(authorEmail); }

    /** Diagram message: SC -> Reviewer.assignReview() */
    public void assignReview(Submission s) {
        InteractionCounter.tick();
        currentWorkload++;
        // No persistence here - assignment lives only in memory in the baseline
    }

    /** Diagram message: EM -> Reviewer.submitScore(score) */
    public int submitScore(Submission s) {
        InteractionCounter.tick();
        // Pseudo-random but deterministic score derived from ids,
        // so behaviour is reproducible across baseline and optimised runs.
        int score = Math.abs((s.getId() + this.id).hashCode()) % 11;  // 0..10
        // Diagram: Reviewer -> Database.saveScore(score)
        db.saveScore(s.getId(), this.id, score);
        return score;
    }
}
