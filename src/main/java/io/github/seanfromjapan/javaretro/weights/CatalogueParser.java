package io.github.seanfromjapan.javaretro.weights;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a gravity catalogue written by hand, in any of three shapes.
 *
 * <pre>
 *   Titan 0.14                     name and factor on one line
 *   Europa
 *   0.134                          name and factor on consecutive lines
 *   The gravity of Ceres is 0.029  a sentence
 * </pre>
 *
 * <p>Blank lines and lines beginning with {@code #} are ignored. Anything else that cannot be read
 * is returned as a {@link ParseProblem} rather than discarded.
 */
public final class CatalogueParser {

  private static final Pattern SENTENCE =
      Pattern.compile("^.*?\\bof\\s+(?<name>.+?)\\s+is\\s+(?<factor>[-+]?[0-9]*\\.?[0-9]+)\\b.*$",
          Pattern.CASE_INSENSITIVE);
  private static final Pattern PAIR =
      Pattern.compile("^(?<name>.*?[^\\s0-9.])\\s+(?<factor>[-+]?[0-9]*\\.?[0-9]+)$");
  private static final Pattern NUMBER = Pattern.compile("^[-+]?[0-9]*\\.?[0-9]+$");
  private static final Pattern NAME = Pattern.compile("^[\\p{L}][\\p{L}0-9 '\\-]*$");

  /** The outcome of a parse: the bodies that were understood, and the lines that were not. */
  public static final class Result {
    private final List<Body> bodies;
    private final List<ParseProblem> problems;

    Result(List<Body> bodies, List<ParseProblem> problems) {
      this.bodies = List.copyOf(bodies);
      this.problems = List.copyOf(problems);
    }

    public List<Body> bodies() {
      return bodies;
    }

    public List<ParseProblem> problems() {
      return problems;
    }

    public boolean isClean() {
      return problems.isEmpty();
    }
  }

  public Result parse(Path path) throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      return parse(reader);
    }
  }

  public Result parse(Reader source) throws IOException {
    List<Body> bodies = new ArrayList<>();
    List<ParseProblem> problems = new ArrayList<>();

    String pendingName = null;
    int pendingLine = 0;
    int lineNumber = 0;

    try (BufferedReader reader = new BufferedReader(source)) {
      String line;
      while ((line = reader.readLine()) != null) {
        lineNumber++;
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
          continue;
        }

        // A name waiting for its factor takes priority: the next usable line
        // must be that number, or the pair is broken and both are reported.
        if (pendingName != null) {
          if (NUMBER.matcher(trimmed).matches()) {
            addBody(bodies, problems, lineNumber, line, pendingName, trimmed);
            pendingName = null;
            continue;
          }
          problems.add(
              new ParseProblem(pendingLine, pendingName, "name has no gravity factor after it"));
          pendingName = null;
        }

        Matcher sentence = SENTENCE.matcher(trimmed);
        if (sentence.matches()) {
          addBody(bodies, problems, lineNumber, line, sentence.group("name"),
              sentence.group("factor"));
          continue;
        }

        Matcher pair = PAIR.matcher(trimmed);
        if (pair.matches()) {
          addBody(bodies, problems, lineNumber, line, pair.group("name"), pair.group("factor"));
          continue;
        }

        if (NAME.matcher(trimmed).matches()) {
          pendingName = trimmed;
          pendingLine = lineNumber;
          continue;
        }

        problems.add(new ParseProblem(lineNumber, line, "not a name, a number or a sentence"));
      }
    }

    if (pendingName != null) {
      problems.add(
          new ParseProblem(pendingLine, pendingName, "name has no gravity factor after it"));
    }
    return new Result(bodies, problems);
  }

  private static void addBody(List<Body> bodies, List<ParseProblem> problems, int lineNumber,
      String line, String rawName, String rawFactor) {
    String name = normaliseName(rawName);
    if (name.isEmpty()) {
      problems.add(new ParseProblem(lineNumber, line, "empty body name"));
      return;
    }
    try {
      bodies.add(new Body(name, Double.parseDouble(rawFactor)));
    } catch (IllegalArgumentException failure) {
      // Covers both an unparseable number and a factor the Body rejects.
      problems.add(new ParseProblem(lineNumber, line, failure.getMessage()));
    }
  }

  private static String normaliseName(String raw) {
    String cleaned = raw.trim().replaceAll("\\s+", " ");
    if (cleaned.isEmpty()) {
      return cleaned;
    }
    return cleaned.substring(0, 1).toUpperCase(Locale.ROOT) + cleaned.substring(1);
  }
}
