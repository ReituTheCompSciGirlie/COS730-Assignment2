package baseline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Database {
    private final Map<String, Submission> submissions = new HashMap<>();
    private final List<Reviewer> reviewerPool = new ArrayList<>();
    private final List<int[]> rawScoreLog = new ArrayList<>(); 
    public Database() {}

    public void seedReviewer(Reviewer r) { reviewerPool.add(r); }

    /**SC -> DB.saveSubmission(data) */
    public String saveSubmission(Submission s) {
        InteractionCounter.tick();
        submissions.put(s.getId(), s);
        return "OK:" + s.getId();
    }

    /**RM -> DB.fetchReviewers() */
    public List<Reviewer> fetchReviewers() {
        InteractionCounter.tick();
        return new ArrayList<>(reviewerPool);
    }

    /**Reviewer -> DB.saveScore(score) */
    public void saveScore(String submissionId, String reviewerId, int score) {
        InteractionCounter.tick();
        rawScoreLog.add(new int[]{submissionId.hashCode(), reviewerId.hashCode(), score});
    }

    public int scoresFor(String submissionId) {
        int n = 0;
        for (int[] e : rawScoreLog) if (e[0] == submissionId.hashCode()) n++;
        return n;
    }
}
