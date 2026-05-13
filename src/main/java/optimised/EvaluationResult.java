package optimised;


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
