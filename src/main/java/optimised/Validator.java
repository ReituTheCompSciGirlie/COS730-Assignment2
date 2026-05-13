package optimised;

public class Validator {
    public ValidationResult validate(Submission s) {
        InteractionCounter.tick();
        if (s == null || s.getTitle() == null || s.getTitle().isBlank())
            return ValidationResult.fail(ValidationResult.Reason.MISSING_TITLE);
        if (s.getAuthorEmail() == null || !s.getAuthorEmail().contains("@"))
            return ValidationResult.fail(ValidationResult.Reason.BAD_EMAIL);
        if (s.getContent() == null || s.getContent().isBlank())
            return ValidationResult.fail(ValidationResult.Reason.EMPTY_CONTENT);
        String f = s.getFormat();
        if (f == null || !(f.equals("PDF") || f.equals("DOCX")))
            return ValidationResult.fail(ValidationResult.Reason.UNSUPPORTED_FORMAT);
        if (s.getPageCount() <= 0 || s.getPageCount() > 50)
            return ValidationResult.fail(ValidationResult.Reason.PAGE_OUT_OF_RANGE);
        return ValidationResult.ok();
    }
}
