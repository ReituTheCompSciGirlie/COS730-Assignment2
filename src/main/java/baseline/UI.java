package baseline;

/**
 * Thin UI lifeline. In a real system this would be a screen / form;
 * here it just forwards the submitResearchOutput() call to the
 * controller and prints any error returned.
 */
public class UI {
    private final SubmissionController controller;

    public UI(SubmissionController c) { this.controller = c; }

    /** Diagram: Researcher -> UI.submitResearchOutput(data) */
    public String submitResearchOutput(Submission s) {
        InteractionCounter.tick();
        return controller.submit(s);
    }
}
