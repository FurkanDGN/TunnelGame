package com.github.furkandgn.tunnelgame.common.util.time;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Predicate;

@SuppressWarnings("StatementWithEmptyBody")
final class TimeScanner {

  private final char[] time;
  private int index = 0;

  TimeScanner(final @NotNull String time) {
    this.time = time.toCharArray();
  }

  boolean hasNext() {
    return this.index < this.time.length - 1;
  }

  long nextLong() {
    return Long.parseLong(String.valueOf(this.next(Character::isDigit)));
  }

  @NotNull String nextString() {
    return String.copyValueOf(this.next(codePoint ->
      Character.isAlphabetic(Integer.valueOf(codePoint))
    ));
  }

  private char[] next(final @NotNull Predicate<Character> condition) {
    final int startIndex = this.index;
    while (++this.index < this.time.length && condition.test(this.time[this.index])) ;
    return Arrays.copyOfRange(this.time, startIndex, this.index);
  }

}