package baseline;

import java.util.List;


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

    /**UI -> SC.submit(data) */
    public String submit(Submission data) {
        InteractionCounter.tick();

        boolean valid = validator.validateFormat(data);
        if (!valid) {

            return "ERROR:INVALID_FORMAT";
        }

        db.saveSubmission(data);
        List<Reviewer> filtered = reviewerManager.getAvailableReviewers(data);

        int n = Math.min(3, filtered.size());
        List<Reviewer> chosen = filtered.subList(0, n);
        for (Reviewer r : chosen) {
            r.assignReview(data);
        }

        return evaluationManager.startEvaluation(data, new java.util.ArrayList<>(chosen));
    }
}
