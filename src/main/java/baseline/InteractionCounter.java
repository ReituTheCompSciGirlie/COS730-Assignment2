package baseline;

/**
 * A simple, deterministic interaction counter used to empirically measure
 * the number of inter-object messages performed by a single execution of
 * the baseline system. Each public method on a participant calls
 * InteractionCounter.tick() once at entry, so the resulting count is the
 * total number of messages exchanged between objects.
 */
public final class InteractionCounter {
    private static long count = 0;

    private InteractionCounter() {}

    public static void reset() { count = 0; }
    public static void tick() { count++; }
    public static long get() { return count; }
}
