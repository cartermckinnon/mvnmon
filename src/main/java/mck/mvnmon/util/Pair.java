package mck.mvnmon.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** An immutable 2-tuple. */
@Getter
@ToString
@EqualsAndHashCode
public class Pair<L, R> {
  private final L left;
  private final R right;

  public Pair(L left, R right) {
    this.left = left;
    this.right = right;
  }
}
