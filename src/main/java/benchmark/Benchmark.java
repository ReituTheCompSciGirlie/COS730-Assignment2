package benchmark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 *
 * Run:
 *   mvn exec:java@benchmark
 * or:
 *   java -cp target/classes benchmark.Benchmark
 */
public class Benchmark {

    private static final int WARMUP_RUNS = 2_000;
    private static final int RUNS_PER_TRIAL = 20_000;
    private static final int TIMING_TRIALS = 5;


    private static final String BASELINE_SRC  = "src/main/java/baseline";
    private static final String OPTIMISED_SRC = "src/main/java/optimised";

    public static void main(String[] args) throws IOException {
        printHeader("Task 6 Empirical Evaluation");

        printMethodology();

        Systems sys = wireUpSystems();

        IntMetric interactions = measureInteractions(sys);

        TimeMetric time = measureTimings(sys);

        CodeMetrics baseCode = scanSources(BASELINE_SRC, "baseline");
        CodeMetrics optCode  = scanSources(OPTIMISED_SRC, "optimised");

        printInteractionsTable(interactions);
        printTimingTable(time);
        printCodeComplexityTable(baseCode, optCode);
        printMaintainabilityTable(baseCode, optCode);
        printFinalSummary(interactions, time, baseCode, optCode);
    }

    // =================================================================
    //  Setup
    // =================================================================

    private static class Systems {
        baseline.UI baseUi;
        optimised.UI optUi;
    }

    private static Systems wireUpSystems() {
        Systems s = new Systems();

        baseline.Database baseDb = new baseline.Database();
        seedBaselineReviewers(baseDb);
        baseline.Validator bV = new baseline.Validator();
        baseline.ReviewerManager bRm = new baseline.ReviewerManager(baseDb);
        baseline.NotificationService bNs = new baseline.NotificationService();
        baseline.EvaluationManager bEm = new baseline.EvaluationManager(bNs);
        baseline.SubmissionController bSc =
                new baseline.SubmissionController(bV, baseDb, bRm, bEm);
        s.baseUi = new baseline.UI(bSc);

        optimised.SubmissionRepository optRepo = new optimised.SubmissionRepository();
        seedOptimisedReviewers(optRepo);
        optimised.Validator oV = new optimised.Validator();
        optimised.ReviewerManager oRm = new optimised.ReviewerManager(optRepo);
        optimised.EvaluationManager oEm = new optimised.EvaluationManager(optRepo);
        optimised.DecisionEngine oDe = new optimised.DecisionEngine();
        optimised.NotificationService oNs = new optimised.NotificationService();
        optimised.SubmissionController oSc =
                new optimised.SubmissionController(oV, optRepo, oRm, oEm, oDe, oNs);
        s.optUi = new optimised.UI(oSc);

        // Warm-up
        for (int i = 0; i < WARMUP_RUNS; i++) {
            s.baseUi.submitResearchOutput(makeBaselineSubmission(i));
            s.optUi.submitResearchOutput(makeOptimisedSubmission(i));
        }
        return s;
    }

    // =================================================================
    //  Measurements
    // =================================================================

    private static class IntMetric {
        long base, opt;
        double pctReduction() { return 100.0 * (base - opt) / base; }
    }

    private static IntMetric measureInteractions(Systems s) {
        IntMetric m = new IntMetric();

        baseline.InteractionCounter.reset();
        s.baseUi.submitResearchOutput(makeBaselineSubmission(0));
        m.base = baseline.InteractionCounter.get();

        optimised.InteractionCounter.reset();
        s.optUi.submitResearchOutput(makeOptimisedSubmission(0));
        m.opt = optimised.InteractionCounter.get();
        return m;
    }

    private static class TimeMetric {
        double[] baseTrialMeansNs;
        double[] optTrialMeansNs;
        double baseMean, baseStdDev, baseMin, baseMax;
        double optMean,  optStdDev,  optMin,  optMax;
        double pctReduction() { return 100.0 * (baseMean - optMean) / baseMean; }
    }

    private static TimeMetric measureTimings(Systems s) {
        TimeMetric m = new TimeMetric();
        m.baseTrialMeansNs = new double[TIMING_TRIALS];
        m.optTrialMeansNs  = new double[TIMING_TRIALS];

        for (int t = 0; t < TIMING_TRIALS; t++) {
            long t0, t1;

            t0 = System.nanoTime();
            for (int i = 0; i < RUNS_PER_TRIAL; i++)
                s.baseUi.submitResearchOutput(makeBaselineSubmission(i));
            t1 = System.nanoTime();
            m.baseTrialMeansNs[t] = (t1 - t0) / (double) RUNS_PER_TRIAL;

            t0 = System.nanoTime();
            for (int i = 0; i < RUNS_PER_TRIAL; i++)
                s.optUi.submitResearchOutput(makeOptimisedSubmission(i));
            t1 = System.nanoTime();
            m.optTrialMeansNs[t] = (t1 - t0) / (double) RUNS_PER_TRIAL;
        }

        m.baseMean = mean(m.baseTrialMeansNs);
        m.optMean  = mean(m.optTrialMeansNs);
        m.baseStdDev = stdDev(m.baseTrialMeansNs, m.baseMean);
        m.optStdDev  = stdDev(m.optTrialMeansNs,  m.optMean);
        m.baseMin = min(m.baseTrialMeansNs); m.baseMax = max(m.baseTrialMeansNs);
        m.optMin  = min(m.optTrialMeansNs);  m.optMax  = max(m.optTrialMeansNs);
        return m;
    }

    private static double mean(double[] a) {
        double s = 0; for (double v : a) s += v; return s / a.length;
    }
    private static double stdDev(double[] a, double mu) {
        double s = 0; for (double v : a) s += (v - mu) * (v - mu);
        return Math.sqrt(s / a.length);
    }
    private static double min(double[] a) { double m = Double.POSITIVE_INFINITY; for (double v : a) if (v < m) m = v; return m; }
    private static double max(double[] a) { double m = Double.NEGATIVE_INFINITY; for (double v : a) if (v > m) m = v; return m; }

    // =================================================================
    //  Code complexity scanner
    // =================================================================

    private static class CodeMetrics {
        String pkg;
        int files;
        int totalLoc;          // total lines of code (production only)
        int totalPublicMethods;
        int totalDecisionPoints;   // sum of if/else if/for/while/case/&&/||
        int maxClassDecisionPoints;
        String mostComplexClass = "(none)";
    }

    private static CodeMetrics scanSources(String dir, String pkg) throws IOException {
        CodeMetrics m = new CodeMetrics();
        m.pkg = pkg;
        Path p = Paths.get(dir);
        if (!Files.exists(p)) {
            // Fall back to common alternate locations so the benchmark works
            // when run from project-root or from the build directory.
            String[] alt = { "../" + dir, "../../" + dir };
            for (String a : alt) {
                if (Files.exists(Paths.get(a))) { p = Paths.get(a); break; }
            }
        }
        if (!Files.exists(p)) {
            System.err.println("Warning: source directory not found: " + dir
                    + " (code-complexity metrics will be reported as 0)");
            return m;
        }

        // Patterns for the lightweight static analysis
        Pattern publicMethod = Pattern.compile(
                "^\\s*public\\s+(?!class|enum|interface)[^;]*\\([^)]*\\)\\s*\\{?",
                Pattern.MULTILINE);
        Pattern decisionPt = Pattern.compile(
                "\\bif\\s*\\(|\\belse\\s+if\\b|\\bfor\\s*\\(|\\bwhile\\s*\\("
              + "|\\bcase\\s+|&&|\\|\\|");

        try (Stream<Path> stream = Files.walk(p)) {
            List<Path> files = stream.filter(f -> f.toString().endsWith(".java")
                                              && !f.getFileName().toString().equals("Main.java")
                                              && !f.getFileName().toString().equals("InteractionCounter.java"))
                                     .sorted()
                                     .collect(java.util.stream.Collectors.toList());
            for (Path f : files) {
                String src = new String(Files.readAllBytes(f));
                int loc = src.split("\n", -1).length;
                m.totalLoc += loc;

                Matcher pm = publicMethod.matcher(src);
                int methods = 0;
                while (pm.find()) methods++;
                m.totalPublicMethods += methods;

                Matcher dp = decisionPt.matcher(src);
                int dpCount = 0;
                while (dp.find()) dpCount++;
                m.totalDecisionPoints += dpCount;

                if (dpCount > m.maxClassDecisionPoints) {
                    m.maxClassDecisionPoints = dpCount;
                    m.mostComplexClass = f.getFileName().toString();
                }
                m.files++;
            }
        }
        return m;
    }

    // =================================================================
    //  Pretty-printing
    // =================================================================

    private static void printHeader(String text) {
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < text.length() + 4; i++) bar.append('=');
        System.out.println();
        System.out.println(bar);
        System.out.println("  " + text);
        System.out.println(bar);
    }

    private static void section(String t) {
        System.out.println();
        System.out.println("--- " + t + " ---");
    }

    private static void printMethodology() {
        section("Methodology");
        System.out.println("  Warm-up iterations            : " + WARMUP_RUNS);
        System.out.println("  Timing trials                 : " + TIMING_TRIALS);
        System.out.println("  Submissions per trial         : " + RUNS_PER_TRIAL);
        System.out.println("  Interaction counter           : 1 tick per message-receiving method");
        System.out.println("  Code complexity proxy         : count of if / else if / for / while / case / && / ||");
        System.out.println("  Hardware                      : " + System.getProperty("os.name") + " "
                + System.getProperty("os.arch") + ", JVM "
                + System.getProperty("java.version"));
    }

    private static void printInteractionsTable(IntMetric m) {
        section("1. Number of method calls / interactions");
        printRow("Quantity", "Baseline", "Optimised", "Change");
        printSep();
        printRow("Interactions per submission",
                String.valueOf(m.base),
                String.valueOf(m.opt),
                String.format("- %.1f %%", m.pctReduction()));
        printRow("Per 50,000 submissions (millions)",
                String.format("%.2f", m.base * 50_000.0 / 1_000_000.0),
                String.format("%.2f", m.opt  * 50_000.0 / 1_000_000.0),
                String.format("- %.1f %%", m.pctReduction()));
    }

    private static void printTimingTable(TimeMetric m) {
        section("2. Execution time (5 trials of 20,000 submissions each)");
        printRow("Quantity", "Baseline", "Optimised", "Change");
        printSep();
        printRow("Mean per submission (ns)",
                String.format("%,.0f", m.baseMean),
                String.format("%,.0f", m.optMean),
                String.format("- %.1f %%", m.pctReduction()));
        printRow("Std dev (ns)",
                String.format("%,.0f", m.baseStdDev),
                String.format("%,.0f", m.optStdDev),
                "");
        printRow("Min trial mean (ns)",
                String.format("%,.0f", m.baseMin),
                String.format("%,.0f", m.optMin),
                "");
        printRow("Max trial mean (ns)",
                String.format("%,.0f", m.baseMax),
                String.format("%,.0f", m.optMax),
                "");
        printRow("Throughput (submissions/sec)",
                String.format("%,.0f", 1e9 / m.baseMean),
                String.format("%,.0f", 1e9 / m.optMean),
                String.format("+ %.1f %%", 100.0 * (1.0/m.optMean - 1.0/m.baseMean) / (1.0/m.baseMean)));
    }

    private static void printCodeComplexityTable(CodeMetrics base, CodeMetrics opt) {
        section("3. Code complexity (static analysis of production source files)");
        printRow("Metric", "Baseline", "Optimised", "Change");
        printSep();
        printRow("Production source files",
                String.valueOf(base.files),
                String.valueOf(opt.files),
                pctChangeStr(base.files, opt.files));
        printRow("Total lines of code",
                String.valueOf(base.totalLoc),
                String.valueOf(opt.totalLoc),
                pctChangeStr(base.totalLoc, opt.totalLoc));
        printRow("Total public methods",
                String.valueOf(base.totalPublicMethods),
                String.valueOf(opt.totalPublicMethods),
                pctChangeStr(base.totalPublicMethods, opt.totalPublicMethods));
        printRow("Total decision points (~CC)",
                String.valueOf(base.totalDecisionPoints),
                String.valueOf(opt.totalDecisionPoints),
                pctChangeStr(base.totalDecisionPoints, opt.totalDecisionPoints));
        printRow("Max class decision points",
                String.valueOf(base.maxClassDecisionPoints),
                String.valueOf(opt.maxClassDecisionPoints),
                pctChangeStr(base.maxClassDecisionPoints, opt.maxClassDecisionPoints));
        printRow("Most complex class",
                base.mostComplexClass,
                opt.mostComplexClass,
                "");
    }

    private static void printMaintainabilityTable(CodeMetrics base, CodeMetrics opt) {
        section("4. Maintainability indicators");
        printRow("Indicator", "Baseline", "Optimised", "Change");
        printSep();
        printRow("Direct collaborators of SubmissionController", "4", "6", "+ 2 (via value objects)");
        printRow("Classes that depend on persistence",            "3", "2", "- 33 %");
        printRow("Public methods on EvaluationManager",            "6", "3", "- 50 %");
        printRow("Public methods on NotificationService",          "6", "3", "- 50 %");
        printRow("Files to edit when adding a new outcome rule",   "4", "2", "- 50 %");
        printRow("Test fixture size for outcome logic (classes)",  "5", "2", "- 60 %");
        printRow("GRASP Information Expert violations",            "1 (Reviewer.saveScore)", "0", "fixed");
    }

   
    private static void printFinalSummary(IntMetric inter, TimeMetric time,
                                          CodeMetrics base, CodeMetrics opt) {
        section("Final summary");
        printRow("Category", "Baseline", "Optimised", "Change");
        printSep();
        printRow("Interactions / submission",
                String.valueOf(inter.base), String.valueOf(inter.opt),
                String.format("- %.1f %%", inter.pctReduction()));
        printRow("Mean time / submission (ns)",
                String.format("%,.0f", time.baseMean),
                String.format("%,.0f", time.optMean),
                String.format("- %.1f %%", time.pctReduction()));
        printRow("Total cyclomatic complexity",
                String.valueOf(base.totalDecisionPoints),
                String.valueOf(opt.totalDecisionPoints),
                pctChangeStr(base.totalDecisionPoints, opt.totalDecisionPoints));
        printRow("Max class CC",
                String.valueOf(base.maxClassDecisionPoints),
                String.valueOf(opt.maxClassDecisionPoints),
                pctChangeStr(base.maxClassDecisionPoints, opt.maxClassDecisionPoints));
        printRow("Production LOC",
                String.valueOf(base.totalLoc),
                String.valueOf(opt.totalLoc),
                pctChangeStr(base.totalLoc, opt.totalLoc));
        System.out.println();
        System.out.println("All four metric categories required by Task 6 of the brief have been");
        System.out.println("reported above. See the technical report (Section 7) for the full");
        System.out.println("written analysis.");
    }

    // =================================================================
    //  Tiny table-formatting helpers
    // =================================================================

    private static void printRow(String c1, String c2, String c3, String c4) {
        System.out.printf("  %-44s  %-12s  %-12s  %s%n", c1, c2, c3, c4);
    }
    private static void printSep() {
        System.out.println("  " +
                "--------------------------------------------" + "  " +
                "------------" + "  " + "------------" + "  " +
                "----------------------");
    }
    private static String pctChangeStr(double base, double opt) {
        if (base == 0) return "n/a";
        double delta = 100.0 * (opt - base) / base;
        return String.format("%s %.1f %%", delta < 0 ? "-" : "+", Math.abs(delta));
    }

    // =================================================================
    //  Submission factories and seeding
    // =================================================================

    private static baseline.Submission makeBaselineSubmission(int i) {
        return new baseline.Submission(
                "B" + i,
                "Submission " + i,
                "researcher" + (i % 4) + "@example.org",
                "Body content for benchmark run #" + i,
                "PDF", 12);
    }

    private static optimised.Submission makeOptimisedSubmission(int i) {
        return new optimised.Submission(
                "O" + i,
                "Submission " + i,
                "researcher" + (i % 4) + "@example.org",
                "Body content for benchmark run #" + i,
                "PDF", 12);
    }

    private static void seedBaselineReviewers(baseline.Database db) {
        for (int i = 1; i <= 6; i++) {
            baseline.Reviewer r = new baseline.Reviewer("R" + i, "Reviewer " + i, i % 4, db);
            if (i == 5) r.addConflict("researcher0@example.org");
            db.seedReviewer(r);
        }
    }

    private static void seedOptimisedReviewers(optimised.SubmissionRepository repo) {
        for (int i = 1; i <= 6; i++) {
            optimised.Reviewer r = new optimised.Reviewer("R" + i, "Reviewer " + i, i % 4);
            if (i == 5) r.addConflict("researcher0@example.org");
            repo.seedReviewer(r);
        }
    }
}