package mck.mvnmon.util;

import java.util.Comparator;

public enum Strings {
  INSTANCE;

  private static final String DIGIT_AND_DECIMAL_REGEX = "[^\\d.]";

  public static Comparator<String> createLengthComparator() {
    return Comparator.comparingInt(String::length);
  }

  public static Comparator<String> createNaturalOrderComparator() {
    return Comparator.comparingDouble(Strings::parseStringToNumber);
  }

  private static double parseStringToNumber(String input) {
    String digitsOnly = input.replaceAll(DIGIT_AND_DECIMAL_REGEX, "");
    if (digitsOnly.isEmpty()) {
      return 0;
    }
    try {
      return Double.parseDouble(digitsOnly);
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }
}
