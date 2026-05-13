package baseline;


public class Validator {

    /**SC -> Validator.validateFormat(data)*/
    public boolean validateFormat(Submission s) {
        InteractionCounter.tick();
        if (s == null) return false;
        if (s.getTitle() == null || s.getTitle().isBlank()) return false;
        if (s.getAuthorEmail() == null || !s.getAuthorEmail().contains("@")) return false;
        if (s.getContent() == null || s.getContent().isBlank()) return false;
        String f = s.getFormat();
        if (f == null) return false;
        if (!(f.equals("PDF") || f.equals("DOCX"))) return false;
        if (s.getPageCount() <= 0 || s.getPageCount() > 50) return false;
        return true;
    }
}
