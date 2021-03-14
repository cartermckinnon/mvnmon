package dev.mck.mvnmon.util;

import dev.mck.mvnmon.util.Strings;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class StringsTest {
  @Test
  public void nthIndexOf() {
    assertThrows(NullPointerException.class, () -> Strings.nthIndexOf(null, 'c', 1));
    assertThrows(IllegalArgumentException.class, () -> Strings.nthIndexOf("", 'c', -1));
    assertThrows(IllegalArgumentException.class, () -> Strings.nthIndexOf("", 'c', 0));

    assertThat(Strings.nthIndexOf("", 'a', 1)).isEqualTo(-1);
    assertThat(Strings.nthIndexOf("a", 'a', 1)).isEqualTo(0);
    assertThat(Strings.nthIndexOf(" a", 'a', 1)).isEqualTo(1);
    assertThat(Strings.nthIndexOf("a", 'a', 2)).isEqualTo(-1);
    assertThat(Strings.nthIndexOf("aa", 'a', 1)).isEqualTo(0);
    assertThat(Strings.nthIndexOf("aa", 'a', 2)).isEqualTo(1);
    assertThat(Strings.nthIndexOf("aaa", 'a', 2)).isEqualTo(1);
    assertThat(Strings.nthIndexOf("aaa", 'a', 3)).isEqualTo(2);
    assertThat(Strings.nthIndexOf("bab", 'a', 1)).isEqualTo(1);
    assertThat(Strings.nthIndexOf("bab", 'a', 2)).isEqualTo(-1);
  }
}
