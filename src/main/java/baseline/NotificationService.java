package baseline;

/**
 * Notification service. Mirrors the diagram which has THREE distinct
 * methods for the three outcome variants (notifyAcceptance,
 * notifyRejection, notifyRevision) plus a sendNotification() back to
 * the researcher. The optimised version collapses these into one
 * polymorphic call.
 */
public class NotificationService {
    private final java.util.List<String> log = new java.util.ArrayList<>();

    public void notifyAcceptance(Submission s) {
        InteractionCounter.tick();
        log.add("ACCEPT:" + s.getId());
    }

    public void notifyRejection(Submission s) {
        InteractionCounter.tick();
        log.add("REJECT:" + s.getId());
    }

    public void notifyRevision(Submission s) {
        InteractionCounter.tick();
        log.add("REVISE:" + s.getId());
    }

    /** Diagram: NS -> Researcher.sendNotification() */
    public void sendNotification(Submission s) {
        InteractionCounter.tick();
        // In a real system this would dispatch over email / SMS.
    }

    public java.util.List<String> getLog() { return log; }
}
