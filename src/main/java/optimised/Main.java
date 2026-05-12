package optimised;

public class Main {
    public static void main(String[] args) {
        SubmissionRepository repo = new SubmissionRepository();
        seedReviewers(repo);

        Validator validator = new Validator();
        ReviewerManager rm = new ReviewerManager(repo);
        EvaluationManager em = new EvaluationManager(repo);
        DecisionEngine de = new DecisionEngine();
        NotificationService ns = new NotificationService();
        SubmissionController sc = new SubmissionController(
                validator, repo, rm, em, de, ns);
        UI ui = new UI(sc);

        Submission s = new Submission(
                "S001",
                "An Empirical Study of GRASP Refactoring",
                "researcher@example.org",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit...",
                "PDF", 14
        );

        InteractionCounter.reset();
        long t0 = System.nanoTime();
        Outcome outcome = ui.submitResearchOutput(s);
        long t1 = System.nanoTime();

        System.out.println("Optimised outcome      : " + outcome);
        System.out.println("Optimised interactions : " + InteractionCounter.get());
        System.out.println("Optimised elapsed (ns) : " + (t1 - t0));
        System.out.println("Notification log       : " + ns.getLog());
    }

    static void seedReviewers(SubmissionRepository repo) {
        for (int i = 1; i <= 6; i++) {
            Reviewer r = new Reviewer("R" + i, "Reviewer " + i, i % 4);
            if (i == 5) r.addConflict("researcher@example.org");
            repo.seedReviewer(r);
        }
    }
}
