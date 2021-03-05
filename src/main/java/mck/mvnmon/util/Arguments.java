package mck.mvnmon.util;

import java.util.Arrays;

public class Arguments {
  public static final String[] shift(String... args) {
    if (args == null) {
      throw new NullPointerException("cannot shift a null arg array");
    }
    if (args.length == 0) {
      return null;
    }
    return Arrays.copyOfRange(args, 1, args.length);
  }
}
