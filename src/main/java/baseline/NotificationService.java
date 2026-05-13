package baseline;


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

    /**NS -> Researcher.sendNotification() */
    public void sendNotification(Submission s) {
        InteractionCounter.tick();
        
    }

    public java.util.List<String> getLog() { return log; }
}
