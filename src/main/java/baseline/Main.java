package baseline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * COS 730 Assignment 2 - Task 1 (Baseline) test harness.
 *
 * Detailed Main that exercises every decision rule documented in
 * section 4 of the report and verifies the empirical metrics from
 * section 7. Each test prints a [PASS] / [FAIL] line and a final
 * summary counts the totals.
 *
 * Run from VSCode : open this file and click "Run" above main(...).
 * Run from CLI    : mvn exec:java@baseline
 *                   (or:  java -cp target/classes baseline.Main )
 *
 * Tests:
 *   1. Validator rules                    -> Decision Table 1 (V1-V5)
 *   2. Reviewer eligibility rules         -> Decision Table 2 (R1-R3)
 *   3. Outcome resolution rules           -> Decision Table 3 (O1-O4)
 *   4. End-to-end happy path
 *   5. End-to-end invalid path
 *   6. Per-submission interaction count   -> Section 7.2 of the report
 */
public class Main {

    private static int passed = 0;
    private static int failed = 0;
    private static final List<String> failureMessages = new ArrayList<>();

    public static void main(String[] args) {
        printHeader("COS 730 Assignment 2  -  Task 1 (Baseline) Test Harness");

        testValidatorRules();
        testReviewerFilteringRules();
        testOutcomeRules();
        testHappyPathPipeline();
        testInvalidPathPipeline();
        testInteractionCount();

        printSummary();
    }

    // ============================================================
    // Test 1 - Validator rules (Decision Table 1: V1-V5)
    // ============================================================
    private static void testValidatorRules() {
        section("Test 1: Validator rules - Decision Table 1 (V1-V5)");
        Validator v = new Validator();

        Submission ok = new Submission("VOK", "Title",
                "researcher@example.org", "body", "PDF", 10);
        assertTrue("OK : all five conditions hold -> valid", v.validateFormat(ok));

        Submission v1 = new Submission("V1", "",
                "researcher@example.org", "body", "PDF", 10);
        assertFalse("V1 : missing title -> invalid", v.validateFormat(v1));

        Submission v2 = new Submission("V2", "Title",
                "not-an-email", "body", "PDF", 10);
        assertFalse("V2 : bad author email -> invalid", v.validateFormat(v2));

        Submission v3 = new Submission("V3", "Title",
                "researcher@example.org", "", "PDF", 10);
        assertFalse("V3 : empty body content -> invalid", v.validateFormat(v3));

        Submission v4 = new Submission("V4", "Title",
                "researcher@example.org", "body", "TXT", 10);
        assertFalse("V4 : unsupported format (TXT) -> invalid", v.validateFormat(v4));

        Submission v5a = new Submission("V5a", "Title",
                "researcher@example.org", "body", "PDF", 0);
        assertFalse("V5 : 0 pages -> invalid", v.validateFormat(v5a));

        Submission v5b = new Submission("V5b", "Title",
                "researcher@example.org", "body", "PDF", 51);
        assertFalse("V5 : 51 pages -> invalid", v.validateFormat(v5b));

        Submission edge50 = new Submission("V5c", "Title",
                "researcher@example.org", "body", "PDF", 50);
        assertTrue("OK : 50 pages on the boundary -> valid", v.validateFormat(edge50));

        Submission docx = new Submission("VD", "Title",
                "researcher@example.org", "body", "DOCX", 10);
        assertTrue("OK : DOCX is also a supported format -> valid", v.validateFormat(docx));
    }

    // ============================================================
    // Test 2 - Reviewer eligibility (Decision Table 2: R1-R3)
    // ============================================================
    private static void testReviewerFilteringRules() {
        section("Test 2: Reviewer eligibility - Decision Table 2 (R1-R3)");
        Database db = new Database();

        Reviewer r1 = new Reviewer("R1", "Alice", 1, db);  // eligible
        Reviewer r2 = new Reviewer("R2", "Bob",   5, db);  // workload at limit
        Reviewer r3 = new Reviewer("R3", "Cara",  1, db);  // conflict
        r3.addConflict("alice@example.org");
        Reviewer r4 = new Reviewer("R4", "Dan",   2, db);  // eligible
        Reviewer r5 = new Reviewer("R5", "Erin",  0, db);  // eligible

        db.seedReviewer(r1);
        db.seedReviewer(r2);
        db.seedReviewer(r3);
        db.seedReviewer(r4);
        db.seedReviewer(r5);

        ReviewerManager rm = new ReviewerManager(db);
        Submission s = new Submission("X", "T", "alice@example.org", "b", "PDF", 10);
        List<Reviewer> filtered = rm.getAvailableReviewers(s);

        assertEquals("Three reviewers remain after filtering (R1, R4, R5)",
                3L, (long) filtered.size());
        assertTrue ("R1 included (low workload, no conflict)",  filtered.contains(r1));
        assertFalse("R2 excluded (workload >= 5)",              filtered.contains(r2));
        assertFalse("R3 excluded (conflict with author)",       filtered.contains(r3));
        assertTrue ("R4 included",                              filtered.contains(r4));
        assertTrue ("R5 included",                              filtered.contains(r5));
    }

    // ============================================================
    // Test 3 - Outcome resolution (Decision Table 3: O1-O4)
    // ============================================================
    private static void testOutcomeRules() {
        section("Test 3: Outcome resolution - Decision Table 3 (O1-O4)");
        Validator v = new Validator();

        Set<String> seen = new TreeSet<>();
        Map<String, String> firstExample = new HashMap<>();
        int scanned = 0;

        for (int i = 0; i < 1000 && seen.size() < 3; i++, scanned++) {
            Database db = new Database();
            seedReviewers(db);
            ReviewerManager rm = new ReviewerManager(db);
            NotificationService ns = new NotificationService();
            EvaluationManager em = new EvaluationManager(ns);
            SubmissionController sc = new SubmissionController(v, db, rm, em);

            String id = "OUT" + i;
            Submission s = new Submission(id, "T",
                    "researcher@example.org", "body", "PDF", 10);
            String outcome = sc.submit(s);
            seen.add(outcome);
            firstExample.putIfAbsent(outcome, id);
        }

        assertTrue("O1 : at least one ACCEPTED outcome observed",  seen.contains("ACCEPTED"));
        assertTrue("O2/O3 : at least one REVISION outcome observed", seen.contains("REVISION"));
        assertTrue("O4 : at least one REJECTED outcome observed",  seen.contains("REJECTED"));

        System.out.println("    Scanned " + scanned + " submissions");
        System.out.println("    Outcomes seen      : " + seen);
        System.out.println("    First example IDs  : " + firstExample);
    }

    // ============================================================
    // Test 4 - End-to-end happy path
    // ============================================================
    private static void testHappyPathPipeline() {
        section("Test 4: End-to-end happy path");
        Database db = new Database();
        seedReviewers(db);
        Validator v = new Validator();
        ReviewerManager rm = new ReviewerManager(db);
        NotificationService ns = new NotificationService();
        EvaluationManager em = new EvaluationManager(ns);
        SubmissionController sc = new SubmissionController(v, db, rm, em);
        UI ui = new UI(sc);

        Submission s = new Submission("S001",
                "An Empirical Study of GRASP Refactoring",
                "researcher@example.org",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit...",
                "PDF", 14);

        InteractionCounter.reset();
        long t0 = System.nanoTime();
        String outcome = ui.submitResearchOutput(s);
        long t1 = System.nanoTime();

        assertTrue("Outcome is one of {ACCEPTED, REJECTED, REVISION}",
                outcome.equals("ACCEPTED")
             || outcome.equals("REJECTED")
             || outcome.equals("REVISION"));
        assertEquals("Submission persisted exactly 3 reviewer scores",
                3L, (long) db.scoresFor("S001"));
        assertEquals("NotificationService logged exactly one notification",
                1L, (long) ns.getLog().size());

        System.out.println("    Outcome              : " + outcome);
        System.out.println("    Notification log     : " + ns.getLog());
        System.out.println("    Interactions counted : " + InteractionCounter.get());
        System.out.println("    Elapsed (ns)         : " + (t1 - t0));
    }

    // ============================================================
    // Test 5 - End-to-end invalid path
    // ============================================================
    private static void testInvalidPathPipeline() {
        section("Test 5: End-to-end invalid path");
        Database db = new Database();
        seedReviewers(db);
        Validator v = new Validator();
        ReviewerManager rm = new ReviewerManager(db);
        NotificationService ns = new NotificationService();
        EvaluationManager em = new EvaluationManager(ns);
        SubmissionController sc = new SubmissionController(v, db, rm, em);
        UI ui = new UI(sc);

        Submission bad = new Submission("BAD", "",
                "researcher@example.org", "body", "PDF", 10);

        InteractionCounter.reset();
        String outcome = ui.submitResearchOutput(bad);

        assertEquals("Invalid submission short-circuits with ERROR:INVALID_FORMAT",
                "ERROR:INVALID_FORMAT", outcome);
        assertEquals("No notification sent for invalid submission",
                0L, (long) ns.getLog().size());
        assertEquals("No score persisted for invalid submission",
                0L, (long) db.scoresFor("BAD"));

        System.out.println("    Interactions on invalid path : "
                + InteractionCounter.get() + " (just UI -> SC -> Validator)");
    }

    // ============================================================
    // Test 6 - Headline metric: interaction count
    // ============================================================
    private static void testInteractionCount() {
        section("Test 6: Per-submission interaction count (report Section 7.2)");
        Database db = new Database();
        seedReviewers(db);
        Validator v = new Validator();
        ReviewerManager rm = new ReviewerManager(db);
        NotificationService ns = new NotificationService();
        EvaluationManager em = new EvaluationManager(ns);
        SubmissionController sc = new SubmissionController(v, db, rm, em);
        UI ui = new UI(sc);

        Submission s = new Submission("METRIC", "Title",
                "researcher@example.org", "body", "PDF", 10);
        InteractionCounter.reset();
        ui.submitResearchOutput(s);
        long count = InteractionCounter.get();

        System.out.println("    Interactions per submission (baseline) : " + count);
        assertEquals("Baseline performs exactly 23 interactions per submission",
                23L, count);
    }

    // ============================================================
    // Helpers
    // ============================================================

    /**
     * Standard reviewer pool used by the integration tests:
     *   R1..R6 with workloads {1, 2, 3, 0, 1, 2}; R5 has a conflict
     *   with researcher@example.org. After filtering the eligible
     *   reviewers are R1, R2, R3, R4, R6; the controller picks the
     *   first three (R1, R2, R3).
     */
    private static void seedReviewers(Database db) {
        for (int i = 1; i <= 6; i++) {
            Reviewer r = new Reviewer("R" + i, "Reviewer " + i, i % 4, db);
            if (i == 5) r.addConflict("researcher@example.org");
            db.seedReviewer(r);
        }
    }

    private static void printHeader(String text) {
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < text.length(); i++) bar.append('=');
        System.out.println(bar);
        System.out.println(text);
        System.out.println(bar);
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("--- " + title + " ---");
    }

    private static void printSummary() {
        System.out.println();
        System.out.println("==========================================");
        System.out.println(" Total  tests : " + (passed + failed));
        System.out.println(" Passed       : " + passed);
        System.out.println(" Failed       : " + failed);
        if (failed > 0) {
            System.out.println();
            System.out.println("Failure details:");
            for (String m : failureMessages) System.out.println("  - " + m);
        }
        System.out.println("==========================================");
    }

    private static void assertTrue(String desc, boolean cond) {
        if (cond) {
            passed++;
            System.out.println("  [PASS] " + desc);
        } else {
            failed++;
            failureMessages.add(desc);
            System.out.println("  [FAIL] " + desc);
        }
    }

    private static void assertFalse(String desc, boolean cond) {
        assertTrue(desc, !cond);
    }

    private static void assertEquals(String desc, Object expected, Object actual) {
        if (Objects.equals(expected, actual)) {
            passed++;
            System.out.println("  [PASS] " + desc);
        } else {
            failed++;
            String msg = desc + " (expected=" + expected + ", actual=" + actual + ")";
            failureMessages.add(msg);
            System.out.println("  [FAIL] " + msg);
        }
    }
}