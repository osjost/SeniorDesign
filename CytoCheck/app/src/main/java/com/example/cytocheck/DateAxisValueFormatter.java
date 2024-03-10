package com.example.cytocheck;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateAxisValueFormatter extends ValueFormatter {

    private final ArrayList<String> timestamps;
    private final SimpleDateFormat sdf;
    private final int intervalType;

    public static final int DAILY = 0;
    public static final int WEEKLY = 1;
    public static final int MONTHLY = 2;

    public DateAxisValueFormatter(ArrayList<String> timestamps, int intervalType) {
        this.timestamps = timestamps;
        this.intervalType = intervalType;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        long millis = (long) value;
        Date date = new Date(millis);

        switch (intervalType) {
            case DAILY:
                return formatDate(date, "HH:mm");
            case WEEKLY:
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(millis);
                cal.add(Calendar.DAY_OF_MONTH, -6);
                Date startDate = cal.getTime();
                return formatDate(startDate, "yyyy-MM-dd") + " - " + formatDate(date, "yyyy-MM-dd");
            case MONTHLY:
                Calendar monthCal = Calendar.getInstance();
                monthCal.setTimeInMillis(millis);
                monthCal.set(Calendar.DAY_OF_MONTH, 1);
                Date startOfMonth = monthCal.getTime();
                return formatDate(startOfMonth, "yyyy-MM-dd") + " - " + formatDate(date, "yyyy-MM-dd");
            default:
                // For daily, display each hour
                return formatDate(date, "yyyy-MM-dd HH:00");
        }
    }

    private String formatDate(Date date, String pattern) {
        SimpleDateFormat sdfFormatted = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdfFormatted.format(date);
    }
}