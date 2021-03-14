package dev.mck.mvnmon.util;

/** A StringBuilder wrapper capable of appending Strings padded with a given character. */
public class PaddedStringBuilder {
  private final StringBuilder buf;

  /** Construct a padded string builder with an empty buffer. */
  public PaddedStringBuilder() {
    this(new StringBuilder());
  }

  /**
   * Construct a padded string builder which will append to the given buffer.
   *
   * @param buf
   */
  public PaddedStringBuilder(StringBuilder buf) {
    this.buf = buf;
  }

  /**
   * Append a String, suffixed with padding.
   *
   * @param s to append
   * @param c to pad with
   * @param desiredLen total length of appended string. If the given string is equal or greater to
   *     this length, no padding will be added.
   * @return this, for method chaining.
   */
  public final PaddedStringBuilder padWith(String s, char c, int desiredLen) {
    if (s.length() == desiredLen) {
      return this;
    }
    buf.append(s);
    append(buf, ' ', desiredLen - s.length());
    return this;
  }

  /**
   * Append a String, prefixed and suffixed with padding.
   *
   * @param s to append
   * @param c to pad with
   * @param desiredLen total length of appended String. If the given string is equal or greater than
   *     this length, no padding will be added.
   * @return this, for method chaining.
   */
  public final PaddedStringBuilder centerWith(String s, char c, int desiredLen) {
    int spaces = desiredLen / 2;
    StringBuilder buf = new StringBuilder();
    append(buf, ' ', spaces);
    buf.append(s);
    append(buf, ' ', spaces + (desiredLen % 2));
    return this;
  }

  /** Append a String, without any padding. */
  public final PaddedStringBuilder append(String s) {
    buf.append(s);
    return this;
  }

  @Override
  public String toString() {
    return buf.toString();
  }

  /**
   * Append n copies of c to s.
   *
   * @param s
   * @param c
   * @param n
   */
  private static void append(StringBuilder s, char c, int n) {
    for (int i = 0; i < n; i++) {
      s.append(c);
    }
  }
}
