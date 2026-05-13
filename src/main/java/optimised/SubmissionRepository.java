package optimised;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class SubmissionRepository {
    private final Map<String, Submission> submissions = new HashMap<>();
    private final List<Reviewer> reviewerPool = new ArrayList<>();
    private final Map<String, List<int[]>> scores = new HashMap<>();

    public void seedReviewer(Reviewer r) { reviewerPool.add(r); }

    public String persist(Submission s) {
        InteractionCounter.tick();
        submissions.put(s.getId(), s);
        return s.getId();
    }

    public List<Reviewer> reviewerPool() {
        InteractionCounter.tick();
        return new ArrayList<>(reviewerPool);
    }

    public void persistScore(String submissionId, String reviewerId, int score) {
        InteractionCounter.tick();
        scores.computeIfAbsent(submissionId, k -> new ArrayList<>())
              .add(new int[]{reviewerId.hashCode(), score});
    }

    public int countScores(String submissionId) {
        return scores.getOrDefault(submissionId, new ArrayList<>()).size();
    }
}
