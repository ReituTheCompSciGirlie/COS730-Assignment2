package baseline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Reviewer manager. Mirrors the baseline diagram exactly:
 *  - SC calls getAvailableReviewers()
 *  - RM calls fetchReviewers() on Database
 *  - RM does TWO separate self-calls: filterConflicts() then checkWorkload()
 *  - RM returns a filtered list to SC, which then assigns reviews itself
 */
public class ReviewerManager {
    private final Database db;
    private static final int MAX_WORKLOAD = 5;

    public ReviewerManager(Database db) { this.db = db; }

    /** Diagram: SC -> RM.getAvailableReviewers() */
    public List<Reviewer> getAvailableReviewers(Submission s) {
        InteractionCounter.tick();
        List<Reviewer> all = db.fetchReviewers();          // first DB hit
        // Two separate passes (cohesive on paper but wasteful in practice -
        // we walk the list twice when one pass would do)
        filterConflicts(all, s);
        checkWorkload(all);
        return all;
    }

    /** Diagram: RM -> RM.filterConflicts(reviewerList) (self-call) */
    public void filterConflicts(List<Reviewer> reviewers, Submission s) {
        InteractionCounter.tick();
        Iterator<Reviewer> it = reviewers.iterator();
        while (it.hasNext()) {
            Reviewer r = it.next();
            if (r.getConflictsWith().contains(s.getAuthorEmail())) it.remove();
        }
    }

    /** Diagram: RM -> RM.checkWorkload(reviewerList) (self-call) */
    public void checkWorkload(List<Reviewer> reviewers) {
        InteractionCounter.tick();
        Iterator<Reviewer> it = reviewers.iterator();
        while (it.hasNext()) {
            if (it.next().getCurrentWorkload() >= MAX_WORKLOAD) it.remove();
        }
    }
}
