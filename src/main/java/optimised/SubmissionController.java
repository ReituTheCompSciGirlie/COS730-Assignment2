package optimised;

import java.util.List;

/**
 * Optimised SubmissionController. Acts as a thin orchestrator only:
 * each step is delegated to a single collaborator and the controller
 * never inspects internal data. Compare with the baseline, which
 * embedded "pick top 3", looped over reviewers, and triggered
 * outcome-specific notifications itself.
 */
public class SubmissionController {
    private final Validator validator;
    private final SubmissionRepository repo;
    private final ReviewerManager reviewerManager;
    private final EvaluationManager evaluationManager;
    private final DecisionEngine decisionEngine;
    private final NotificationService notifier;

    public SubmissionController(Validator v, SubmissionRepository r,
                                ReviewerManager rm, EvaluationManager em,
                                DecisionEngine de, NotificationService ns) {
        this.validator = v; this.repo = r;
        this.reviewerManager = rm; this.evaluationManager = em;
        this.decisionEngine = de; this.notifier = ns;
    }

    public Outcome submit(Submission data) {
        InteractionCounter.tick();
        ValidationResult vr = validator.validate(data);
        if (!vr.isValid()) return Outcome.INVALID_SUBMISSION;

        repo.persist(data);
        List<Reviewer> assigned = reviewerManager.assignReviewers(data);
        EvaluationResult er = evaluationManager.evaluate(data, assigned);
        Outcome outcome = decisionEngine.resolveOutcome(er);
        notifier.notify(data.getAuthorEmail(), data, outcome);
        return outcome;
    }
}
