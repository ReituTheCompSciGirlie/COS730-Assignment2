package baseline;

import java.util.ArrayList;
import java.util.List;


public class EvaluationManager {
    private final NotificationService notifier;
    private double lastAverage;
    private boolean lastConsensus;
    private String lastOutcome;     
    private final List<Integer> lastScores = new ArrayList<>();

    public EvaluationManager(NotificationService notifier) {
        this.notifier = notifier;
    }

    /**SC -> EM.startEvaluation() */
    public String startEvaluation(Submission s, List<Reviewer> reviewers) {
        InteractionCounter.tick();
        lastScores.clear();
      
        for (Reviewer r : reviewers) {
            int score = r.submitScore(s);   
            lastScores.add(score);
        }
        
        calculateAverage();
        checkConsensus();
        applyRules();

       
        switch (lastOutcome) {
            case "ACCEPTED":
                notifier.notifyAcceptance(s);
                break;
            case "REJECTED":
                notifier.notifyRejection(s);
                break;
            case "REVISION":
                notifier.notifyRevision(s);
                break;
        }
        notifier.sendNotification(s);
        return lastOutcome;
    }

    /**EM -> EM.calculateAverage() */
    public void calculateAverage() {
        InteractionCounter.tick();
        if (lastScores.isEmpty()) { lastAverage = 0.0; return; }
        int sum = 0;
        for (int v : lastScores) sum += v;
        lastAverage = sum / (double) lastScores.size();
    }

    /**EM -> EM.checkConsensus() */
    public void checkConsensus() {
        InteractionCounter.tick();
        if (lastScores.size() < 2) { lastConsensus = true; return; }
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int v : lastScores) { if (v < min) min = v; if (v > max) max = v; }
        // "Consensus" = spread <= 3 points on the 0..10 scale
        lastConsensus = (max - min) <= 3;
    }

    /**EM -> EM.applyRules() */
    public void applyRules() {
        InteractionCounter.tick();
        // Decision logic scattered here - this is what the decision table
        // in Task 3 will replace.
        if (lastAverage >= 7.0 && lastConsensus) {
            lastOutcome = "ACCEPTED";
        } else if (lastAverage < 4.0) {
            lastOutcome = "REJECTED";
        } else if (lastAverage >= 4.0 && lastAverage < 7.0) {
            lastOutcome = "REVISION";
        } else if (lastAverage >= 7.0 && !lastConsensus) {
            // High score but no consensus - send for revision
            lastOutcome = "REVISION";
        } else {
            lastOutcome = "REVISION";
        }
    }
}
