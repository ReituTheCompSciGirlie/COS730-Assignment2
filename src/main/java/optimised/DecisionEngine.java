package optimised;


public class DecisionEngine {

    private static final double ACCEPT_THRESHOLD = 7.0;
    private static final double REJECT_THRESHOLD = 4.0;

    
    private interface Rule {
        boolean matches(EvaluationResult r);
        Outcome outcome();
    }

    private static final Rule[] RULES = new Rule[]{
        new Rule() {
            public boolean matches(EvaluationResult r) {
                return r.getAverage() >= ACCEPT_THRESHOLD && r.hasConsensus();
            }
            public Outcome outcome() { return Outcome.ACCEPTED; }
        },
        
        new Rule() {
            public boolean matches(EvaluationResult r) {
                return r.getAverage() >= ACCEPT_THRESHOLD && !r.hasConsensus();
            }
            public Outcome outcome() { return Outcome.REVISION; }
        },
        
        new Rule() {
            public boolean matches(EvaluationResult r) {
                return r.getAverage() >= REJECT_THRESHOLD
                    && r.getAverage() <  ACCEPT_THRESHOLD;
            }
            public Outcome outcome() { return Outcome.REVISION; }
        },
        
        new Rule() {
            public boolean matches(EvaluationResult r) {
                return r.getAverage() < REJECT_THRESHOLD;
            }
            public Outcome outcome() { return Outcome.REJECTED; }
        },
    };

    public Outcome resolveOutcome(EvaluationResult r) {
        InteractionCounter.tick();
        for (Rule rule : RULES) {
            if (rule.matches(r)) return rule.outcome();
        }
        return Outcome.REVISION;
    }
}
