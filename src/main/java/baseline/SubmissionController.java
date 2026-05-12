package baseline;

import java.util.List;

/**
 * Submission controller (a Controller in the GRASP sense, but in the
 * baseline it is bloated: it knows about Validator, Database,
 * ReviewerManager, Reviewer, AND EvaluationManager directly. That
 * tight coupling is a target for refactoring in the optimised version.
 */
public class SubmissionController {
    private final Validator validator;
    private final Database db;
    private final ReviewerManager reviewerManager;
    private final EvaluationManager evaluationManager;

    public SubmissionController(Validator v, Database db,
                                ReviewerManager rm, EvaluationManager em) {
        this.validator = v;
        this.db = db;
        this.reviewerManager = rm;
        this.evaluationManager = em;
    }

    /** Diagram: UI -> SC.submit(data) */
    public String submit(Submission data) {
        InteractionCounter.tick();

        // SC -> Validator.validateFormat
        boolean valid = validator.validateFormat(data);
        if (!valid) {
            // alt [invalid] -> return error
            return "ERROR:INVALID_FORMAT";
        }

        // SC -> DB.saveSubmission
        db.saveSubmission(data);

        // SC -> RM.getAvailableReviewers
        List<Reviewer> filtered = reviewerManager.getAvailableReviewers(data);

        // loop [assign reviewers]: SC -> Reviewer.assignReview
        // Pick the top three (or all available) - assignment happens at SC level
        int n = Math.min(3, filtered.size());
        List<Reviewer> chosen = filtered.subList(0, n);
        for (Reviewer r : chosen) {
            r.assignReview(data);
        }

        // SC -> EM.startEvaluation
        return evaluationManager.startEvaluation(data, new java.util.ArrayList<>(chosen));
    }
}
