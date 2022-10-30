package com.github.furkandgn.tunnelgame.common.util.game;

import java.util.SplittableRandom;

public final class RandomUtil {

  private static final SplittableRandom RANDOM = new SplittableRandom();

  public static SplittableRandom random() {
    return RANDOM;
  }

  public static long randomLong() {
    return RANDOM.nextLong();
  }

  public static int randomIntInRange(final int range) {
    return RANDOM.nextInt(range);
  }

  public static int randomIntInRange(final int origin, final int range) {
    return RANDOM.nextInt(origin, range);
  }

}
