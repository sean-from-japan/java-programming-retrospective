package io.github.seanfromjapan.javaretro.weights;

/**
 * One line that could not be understood, kept with its position and reason.
 *
 * <p>The point of this type is that a bad line is <em>reported</em>. The version of this program I
 * submitted as coursework swallowed every parse failure in an empty catch block, so a typo in the
 * data file produced a table that was silently one row short.
 */
public final class ParseProblem {

  private final int lineNumber;
  private final String line;
  private final String reason;

  public ParseProblem(int lineNumber, String line, String reason) {
    this.lineNumber = lineNumber;
    this.line = line;
    this.reason = reason;
  }

  public int lineNumber() {
    return lineNumber;
  }

  public String line() {
    return line;
  }

  public String reason() {
    return reason;
  }

  @Override
  public String toString() {
    return "line " + lineNumber + ": " + reason + " -- " + line.trim();
  }
}
