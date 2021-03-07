package mck.mvnmon.util;

public class PaddedStringBuilder {
  private final StringBuilder buf = new StringBuilder();

  public final PaddedStringBuilder padWith(String s, char c, int desiredLen) {
    if (s.length() == desiredLen) {
      return this;
    }
    buf.append(s);
    append(buf, ' ', desiredLen - s.length());
    return this;
  }

  public final PaddedStringBuilder centerWith(String s, char c, int desiredLen) {
    int spaces = desiredLen / 2;
    StringBuilder buf = new StringBuilder();
    append(buf, ' ', spaces);
    buf.append(s);
    append(buf, ' ', spaces + (desiredLen % 2));
    return this;
  }

  public final PaddedStringBuilder append(String s) {
    buf.append(s);
    return this;
  }

  public String toString() {
    return buf.toString();
  }

  private static void append(StringBuilder s, char c, int n) {
    for (int i = 0; i < n; i++) {
      s.append(c);
    }
  }
}
