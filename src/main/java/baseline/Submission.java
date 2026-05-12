package baseline;

import java.util.HashMap;
import java.util.Map;

/**
 * Plain submission data carrier (researcher artefact).
 */
public class Submission {
    private final String id;
    private final String title;
    private final String authorEmail;
    private final String content;
    private final String format;        // e.g. "PDF", "DOCX"
    private final int pageCount;
    private final Map<String, Object> metadata = new HashMap<>();

    public Submission(String id, String title, String authorEmail,
                      String content, String format, int pageCount) {
        this.id = id;
        this.title = title;
        this.authorEmail = authorEmail;
        this.content = content;
        this.format = format;
        this.pageCount = pageCount;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthorEmail() { return authorEmail; }
    public String getContent() { return content; }
    public String getFormat() { return format; }
    public int getPageCount() { return pageCount; }
    public Map<String, Object> getMetadata() { return metadata; }
}
