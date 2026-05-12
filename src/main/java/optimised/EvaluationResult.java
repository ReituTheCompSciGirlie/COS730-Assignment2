package optimised;

/**
 * Aggregated evaluation result. The controller no longer has to know
 * how the average / consensus were derived - it sees only the result.
 */
public class EvaluationResult {
    private final double average;
    private final boolean consensus;
    private final int reviewerCount;

    public EvaluationResult(double average, boolean consensus, int reviewerCount) {
        this.average = average;
        this.consensus = consensus;
        this.reviewerCount = reviewerCount;
    }

    public double getAverage() { return average; }
    public boolean hasConsensus() { return consensus; }
    public int getReviewerCount() { return reviewerCount; }
}
