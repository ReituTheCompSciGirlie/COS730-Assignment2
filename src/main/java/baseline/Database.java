package baseline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory stand-in for a real database. Mirrors the diagram's
 * Database lifeline. Note: in the baseline, both SubmissionController
 * AND ReviewerManager AND individual Reviewers all hold references to
 * this object - that is one of the high-coupling problems we discuss.
 */
public class Database {
    private final Map<String, Submission> submissions = new HashMap<>();
    private final List<Reviewer> reviewerPool = new ArrayList<>();
    private final List<int[]> rawScoreLog = new ArrayList<>(); // submission/reviewer/score

    public Database() {}

    public void seedReviewer(Reviewer r) { reviewerPool.add(r); }

    /** Diagram: SC -> DB.saveSubmission(data) */
    public String saveSubmission(Submission s) {
        InteractionCounter.tick();
        submissions.put(s.getId(), s);
        return "OK:" + s.getId();
    }

    /** Diagram: RM -> DB.fetchReviewers() */
    public List<Reviewer> fetchReviewers() {
        InteractionCounter.tick();
        // Return a fresh ArrayList so callers can mutate it safely
        return new ArrayList<>(reviewerPool);
    }

    /** Diagram: Reviewer -> DB.saveScore(score) */
    public void saveScore(String submissionId, String reviewerId, int score) {
        InteractionCounter.tick();
        // Score log uses a flat int[] just to keep it lightweight; the
        // baseline doesn't bother giving this a domain object.
        rawScoreLog.add(new int[]{submissionId.hashCode(), reviewerId.hashCode(), score});
    }

    public int scoresFor(String submissionId) {
        int n = 0;
        for (int[] e : rawScoreLog) if (e[0] == submissionId.hashCode()) n++;
        return n;
    }
}
