package baseline;


public final class InteractionCounter {
    private static long count = 0;

    private InteractionCounter() {}

    public static void reset() { count = 0; }
    public static void tick() { count++; }
    public static long get() { return count; }
}
