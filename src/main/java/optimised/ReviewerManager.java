package optimised;

import java.util.ArrayList;
import java.util.List;


public class ReviewerManager {
    private final SubmissionRepository repo;
    private static final int MAX_WORKLOAD = 5;
    private static final int TARGET_REVIEWERS = 3;

    public ReviewerManager(SubmissionRepository repo) { this.repo = repo; }

    public List<Reviewer> assignReviewers(Submission s) {
        InteractionCounter.tick();
        List<Reviewer> pool = repo.reviewerPool();
        List<Reviewer> chosen = new ArrayList<>(TARGET_REVIEWERS);
        // Single linear pass: filter and select in one sweep
        for (Reviewer r : pool) {
            if (chosen.size() >= TARGET_REVIEWERS) break;
            if (r.getCurrentWorkload() >= MAX_WORKLOAD) continue;
            if (r.getConflictsWith().contains(s.getAuthorEmail())) continue;
            r.incrementWorkload();
            chosen.add(r);
        }
        return chosen;
    }
}
