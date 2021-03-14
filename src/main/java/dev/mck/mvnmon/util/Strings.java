package dev.mck.mvnmon.util;

/** Utilities for Strings. */
public enum Strings {
  INSTANCE;

  /**
   * @param s a string.
   * @param c a character to look for.
   * @param n the number of character occurrances to look for.
   * @return the index of the n-th occurrance of c, or -1 if there are less than n occurrances of c.
   */
  public static int nthIndexOf(String s, char c, int n) {
    if (s == null) {
      throw new NullPointerException("s cannot be null");
    }
    if (n <= 0) {
      throw new IllegalArgumentException("n must be greater than zero: " + n);
    }
    if (s.isEmpty()) {
      return -1;
    }
    char[] chars = s.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (chars[i] == c && --n == 0) {
        return i;
      }
    }
    return -1;
  }
}
