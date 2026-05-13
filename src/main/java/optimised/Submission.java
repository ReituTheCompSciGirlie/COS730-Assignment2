package optimised;

import java.util.HashMap;
import java.util.Map;


public class Submission {
    private final String id, title, authorEmail, content, format;
    private final int pageCount;
    private final Map<String, Object> metadata = new HashMap<>();

    public Submission(String id, String title, String authorEmail,
                      String content, String format, int pageCount) {
        this.id = id; this.title = title; this.authorEmail = authorEmail;
        this.content = content; this.format = format; this.pageCount = pageCount;
    }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthorEmail() { return authorEmail; }
    public String getContent() { return content; }
    public String getFormat() { return format; }
    public int getPageCount() { return pageCount; }
    public Map<String, Object> getMetadata() { return metadata; }
}
