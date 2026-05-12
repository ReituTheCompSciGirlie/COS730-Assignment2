package optimised;

/**
 * Single enum capturing every possible outcome of the review pipeline.
 * Replaces the three separate notify* methods in the baseline and
 * removes the alt-block in the diagram.
 */
public enum Outcome {
    ACCEPTED,
    REJECTED,
    REVISION,
    INVALID_SUBMISSION
}
