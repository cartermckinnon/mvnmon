package dev.mck.mvnmon.util;

/**
 * Keeps track of the maximum integer value passed to {@link MaximumInt#consider}. The initial value
 * is {@link Integer#MIN_VALUE}.
 */
public class MaximumInt {
  private int i = Integer.MIN_VALUE;

  public MaximumInt consider(int i) {
    this.i = Math.max(this.i, i);
    return this;
  }

  public int get() {
    return i;
  }

  @Override
  public String toString() {
    return Integer.toString(i);
  }
}
