package baseline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ReviewerManager {
    private final Database db;
    private static final int MAX_WORKLOAD = 5;

    public ReviewerManager(Database db) { this.db = db; }

    /**SC -> RM.getAvailableReviewers() */
    public List<Reviewer> getAvailableReviewers(Submission s) {
        InteractionCounter.tick();
        List<Reviewer> all = db.fetchReviewers();          
        filterConflicts(all, s);
        checkWorkload(all);
        return all;
    }

    /**RM.filterConflicts(reviewerList) */
    public void filterConflicts(List<Reviewer> reviewers, Submission s) {
        InteractionCounter.tick();
        Iterator<Reviewer> it = reviewers.iterator();
        while (it.hasNext()) {
            Reviewer r = it.next();
            if (r.getConflictsWith().contains(s.getAuthorEmail())) it.remove();
        }
    }

    /**RM.checkWorkload(reviewerList) (self-call) */
    public void checkWorkload(List<Reviewer> reviewers) {
        InteractionCounter.tick();
        Iterator<Reviewer> it = reviewers.iterator();
        while (it.hasNext()) {
            if (it.next().getCurrentWorkload() >= MAX_WORKLOAD) it.remove();
        }
    }
}
