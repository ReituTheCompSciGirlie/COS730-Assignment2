package optimised;

/**
 * Decision engine driven by an explicit decision table. Each row
 * encodes one rule from Task 3. Adding a new outcome rule means
 * adding a new row, not patching nested if/else logic.
 *
 * Decision table (limited entry):
 *
 *   R1  R2  R3  R4
 *   --  --  --  --
 *   avg >= 7.0      T   T   F   F
 *   spread <= 3     T   F   ?   ?
 *   avg in [4,7)    -   -   T   F
 *   ----------------------------------
 *   ACCEPTED        X
 *   REVISION            X   X
 *   REJECTED                    X
 */
public class DecisionEngine {

    private static final double ACCEPT_THRESHOLD = 7.0;
    private static final double REJECT_THRESHOLD = 4.0;

    /**
     * Each row matches in declaration order; the first match wins.
     * Conditions return true when the row applies. This is the
     * runtime form of the decision table.
     */
    private interface Rule {
        boolean matches(EvaluationResult r);
        Outcome outcome();
    }

    private static final Rule[] RULES = new Rule[]{
        // R1: high avg, with consensus -> ACCEPT
        new Rule() {
            public boolean matches(EvaluationResult r) {
                return r.getAverage() >= ACCEPT_THRESHOLD && r.hasConsensus();
            }
            public Outcome outcome() { return Outcome.ACCEPTED; }
        },
        // R2: high avg but no consensus -> REVISION
        new Rule() {
            public boolean matches(EvaluationResult r) {
                return r.getAverage() >= ACCEPT_THRESHOLD && !r.hasConsensus();
            }
            public Outcome outcome() { return Outcome.REVISION; }
        },
        // R3: middling avg -> REVISION
        new Rule() {
            public boolean matches(EvaluationResult r) {
                return r.getAverage() >= REJECT_THRESHOLD
                    && r.getAverage() <  ACCEPT_THRESHOLD;
            }
            public Outcome outcome() { return Outcome.REVISION; }
        },
        // R4: low avg -> REJECT
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
        // Default fallback - in a real system this would be logged
        return Outcome.REVISION;
    }
}
