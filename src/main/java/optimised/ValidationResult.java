package optimised;

/**
 * A richer return type so the controller no longer has to play
 * "guess the reason" when the boolean returned is false.
 */
public class ValidationResult {
    public enum Reason { OK, MISSING_TITLE, BAD_EMAIL, EMPTY_CONTENT,
                         UNSUPPORTED_FORMAT, PAGE_OUT_OF_RANGE }

    private final boolean valid;
    private final Reason reason;

    private ValidationResult(boolean v, Reason r) { valid = v; reason = r; }

    public static ValidationResult ok() { return new ValidationResult(true, Reason.OK); }
    public static ValidationResult fail(Reason r) { return new ValidationResult(false, r); }

    public boolean isValid() { return valid; }
    public Reason getReason() { return reason; }
}
