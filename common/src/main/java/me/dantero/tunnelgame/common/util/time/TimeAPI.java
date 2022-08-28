package me.dantero.tunnelgame.common.util.time;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class TimeAPI {

  private static final int DAYS_IN_WEEK = 7;
  private static final int DAYS_IN_MONTH = 30;
  private static final int DAYS_IN_YEAR = 365;

  private static final int SECONDS_IN_HOUR = 3600;
  private static final int SECONDS_IN_MINUTE = 60;

  @NotNull
  public String formatTime(Long input) {
    final StringBuilder result = new StringBuilder();

    if (input != null) {
      int value = input.intValue();

      final int hours = value / SECONDS_IN_HOUR;
      if (hours != 0) result.append(String.format("%sh ", hours));

      final int minutes = (value % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE;
      if (minutes != 0) result.append(String.format("%sm ", minutes));

      final int seconds = value % SECONDS_IN_MINUTE;
      if (seconds != 0) result.append(String.format("%ss", seconds));
    }

    return result.toString();
  }

  @NotNull
  public TimeResult parseTime(final String input) {
    if (input == null) return new TimeResult(0L);

    long seconds = 0L;
    final TimeScanner scanner = new TimeScanner(
      input
        .replace(" ", "")
        .replace("and", "")
        .replace(",", "")
        .toLowerCase()
    );

    long next;
    while (scanner.hasNext()) {
      next = scanner.nextLong();
      switch (TimeType.getType(scanner.nextString())) {
        case SECOND -> seconds += next;
        case MINUTE -> seconds += TimeUnit.MINUTES.toSeconds(next);
        case HOUR -> seconds += TimeUnit.HOURS.toSeconds(next);
        case DAY -> seconds += TimeUnit.DAYS.toSeconds(next);
        case WEEK -> seconds += TimeUnit.DAYS.toSeconds(next * DAYS_IN_WEEK);
        case MONTH -> seconds += TimeUnit.DAYS.toSeconds(next * DAYS_IN_MONTH);
        case YEAR -> seconds += TimeUnit.DAYS.toSeconds(next * DAYS_IN_YEAR);
      }
    }

    return new TimeResult(seconds);
  }

  private enum TimeType {

    SECOND("s", "sec", "secs", "second", "seconds"),
    MINUTE("m", "min", "mins", "minute", "minutes"),
    HOUR("h", "hr", "hrs", "hour", "hours"),
    DAY("d", "dy", "dys", "day", "days"),
    WEEK("w", "week", "weeks"),
    MONTH("mo", "mon", "mnth", "month", "months"),
    YEAR("y", "yr", "yrs", "year", "years");

    final Set<String> matches;

    TimeType(final String... matches) {
      this.matches = new HashSet<>(Arrays.asList(matches));
    }

    static TimeType getType(final String input) {
      TimeType type = null;

      for (final TimeType timeType : values()) {
        if (timeType.matches.contains(input)) {
          type = timeType;
          break;
        }
      }

      if (type == null)
        throw new IllegalArgumentException("Failed to find Time Type for String '" + input + "'!");

      return type;
    }

  }

  public static class TimeResult {

    private final Long seconds;

    TimeResult(final Long seconds) {
      this.seconds = seconds;
    }

    public Long to(final TimeUnit unit) {
      return unit.convert(this.seconds, TimeUnit.SECONDS);
    }

  }

}
