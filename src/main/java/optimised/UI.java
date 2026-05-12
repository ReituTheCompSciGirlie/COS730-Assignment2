package optimised;

public class UI {
    private final SubmissionController controller;
    public UI(SubmissionController c) { this.controller = c; }

    public Outcome submitResearchOutput(Submission s) {
        InteractionCounter.tick();
        return controller.submit(s);
    }
}
