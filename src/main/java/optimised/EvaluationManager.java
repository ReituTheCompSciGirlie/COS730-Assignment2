package optimised;

import java.util.List;

/**
 * Evaluation manager. Owns scoring AND persistence of those scores
 * (Information Expert). Computes average/consensus in a single pass
 * over the score list. Returns a structured EvaluationResult instead
 * of branching internally - the outcome decision is delegated to
 * the DecisionEngine.
 */
public class EvaluationManager {
    private static final double CONSENSUS_SPREAD = 3.0;
    private final SubmissionRepository repo;

    public EvaluationManager(SubmissionRepository repo) { this.repo = repo; }

    public EvaluationResult evaluate(Submission s, List<Reviewer> reviewers) {
        InteractionCounter.tick();
        if (reviewers.isEmpty())
            return new EvaluationResult(0.0, true, 0);

        int sum = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (Reviewer r : reviewers) {
            int v = r.score(s);
            sum += v;
            if (v < min) min = v;
            if (v > max) max = v;
            // EM persists - the Reviewer no longer touches the database
            repo.persistScore(s.getId(), r.getId(), v);
        }
        double avg = sum / (double) reviewers.size();
        boolean consensus = (max - min) <= CONSENSUS_SPREAD;
        return new EvaluationResult(avg, consensus, reviewers.size());
    }
}
