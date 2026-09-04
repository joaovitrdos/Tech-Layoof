package com.layoof.layoof.enums;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum NotificationFrequency {

    NONE(Set.of(), Set.of()),
    INSTANT(Set.of(LocalTime.of(10, 0), LocalTime.of(17, 0)), EnumSet.allOf(DayOfWeek.class)),
    DAILY(Set.of(LocalTime.of(10, 0)), EnumSet.allOf(DayOfWeek.class)),
    WEEKLY(Set.of(LocalTime.of(10, 0)), Set.of(DayOfWeek.WEDNESDAY));

    private final Set<LocalTime> times;
    private final Set<DayOfWeek> days;

    NotificationFrequency(Set<LocalTime> times, Set<DayOfWeek> days) {
        this.times = times;
        this.days = days;
    }

    public boolean sendsAt(LocalDateTime moment) {
        return days.contains(moment.getDayOfWeek())
                && times.contains(moment.toLocalTime().truncatedTo(ChronoUnit.MINUTES));
    }

    public static Set<NotificationFrequency> sendingAt(LocalDateTime moment) {
        return Arrays.stream(values())
                .filter(frequency -> frequency.sendsAt(moment))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(NotificationFrequency.class)));
    }
}
