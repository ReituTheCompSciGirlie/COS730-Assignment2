package baseline;


public class UI {
    private final SubmissionController controller;

    public UI(SubmissionController c) { this.controller = c; }

    /** Researcher -> UI.submitResearchOutput(data) */
    public String submitResearchOutput(Submission s) {
        InteractionCounter.tick();
        return controller.submit(s);
    }
}
