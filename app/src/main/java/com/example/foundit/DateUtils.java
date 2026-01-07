package com.example.foundit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public static String format(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }

    public static String format(Date date) {
        return DATE_FORMAT.format(date);
    }
}
