package com.flightsearch.app.utils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationParser {

    private static final Pattern PATTERN = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?");

    public static String parse(String isoDuration) {
        if (isoDuration == null || isoDuration.isEmpty()) return "";
        Matcher m = PATTERN.matcher(isoDuration);
        if (!m.matches()) return isoDuration;
        String hoursStr = m.group(1);
        String minutesStr = m.group(2);
        int hours = hoursStr != null ? Integer.parseInt(hoursStr) : 0;
        int minutes = minutesStr != null ? Integer.parseInt(minutesStr) : 0;
        return fromHoursMinutes(hours, minutes);
    }

    public static String fromHoursMinutes(int hours, int minutes) {
        if (hours > 0 && minutes > 0) {
            return String.format(Locale.US, "%dh %02dm", hours, minutes);
        } else if (hours > 0) {
            return hours + "h";
        } else {
            return minutes + "m";
        }
    }
}
