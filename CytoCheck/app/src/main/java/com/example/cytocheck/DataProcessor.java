package com.example.cytocheck;

import android.graphics.Color;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.components.XAxis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DataProcessor {

    public static void processData(String jsonData, BarChart barChart, String timeFrame) {
        List<Entry> entries = parseJsonData(jsonData);
        Map<String, List<Entry>> data;
        Map<String, List<Entry>> dailyData;
        Map<String, List<Entry>> weeklyData;

        switch (timeFrame) {
            case "Weekly":
                data = aggregateDataByWeek(entries);
                break;
            case "Monthly":
                dailyData = aggregateDataByDay(entries);
                weeklyData = aggregateDataByWeek(entries);
                data = aggregateDataByMonth(entries);

                for (String date : dailyData.keySet()) {
                    Log.d("Date: " , date);
                    List<Entry> entries1 = dailyData.get(date);
                    for (Entry entry : entries1) {
                        Log.d("entry", entry.toString());
                    }
                }

                for (String weekStartDate : weeklyData.keySet()) {
                    Log.d("Week Start Date: " , weekStartDate);
                    List<Entry> entries2 = weeklyData.get(weekStartDate);
                    for (Entry entry : entries2) {
                        Log.d("entry", entry.toString());
                    }
                }
                break;
            default:
                data = aggregateDataByDay(entries);
                break;
        }

        plotData(barChart, data);
    }

    private static List<Entry> parseJsonData(String jsonData) {
        List<Entry> entries = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int nausea = jsonObject.getInt("nausea");
                int fatigue = jsonObject.getInt("fatigue");
                int pain = jsonObject.getInt("pain");
                String timestamp = jsonObject.getString("time_stamp");

                entries.add(new Entry(timestamp, nausea, fatigue, pain));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return entries;
    }
    private static List<QuanEntry> parseJsonQuanData(String jsonData) {
        List<QuanEntry> entries = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int data = jsonObject.getDouble("")
            }
        }
    }

    private static Map<String, List<Entry>> aggregateDataByDay(List<Entry> entries) {
        Map<String, List<Entry>> dailyData = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(calendar.getTime()); // Get the current date

        for (Entry entry : entries) {
            String date = entry.getFormattedDate("yyyy-MM-dd");
            if (date.equals(currentDate)) {
                if (!dailyData.containsKey(date)) {
                    dailyData.put(date, new ArrayList<>());
                }
                dailyData.get(date).add(entry);
            }
        }
        return dailyData;
    }

    private static Map<String, List<Entry>> aggregateDataByWeek(List<Entry> entries) {
        Map<String, List<Entry>> weeklyData = new HashMap<>();
        Calendar calendar = Calendar.getInstance();

        // Set the end date to today
        Date endDate = calendar.getTime();

        // Set the start date to 7 days before today
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date startDate = calendar.getTime();

        // Iterate over the entries and aggregate data for each day
        while (!endDate.before(startDate)) {
            String dateString = formatDate(calendar.getTime(), "yyyy-MM-dd");
            List<Entry> dailyEntries = new ArrayList<>();

            // Collect entries for the current day
            for (Entry entry : entries) {
                if (entry.isWithinDateRange(startDate, endDate)) {
                    dailyEntries.add(entry);
                }
            }

            // Calculate average data for the day
            float sumNausea = 0;
            float sumFatigue = 0;
            float sumPain = 0;
            for (Entry entry : dailyEntries) {
                sumNausea += entry.getNausea();
                sumFatigue += entry.getFatigue();
                sumPain += entry.getPain();
            }

            // Add the averaged data to the weeklyData map
            if (!dailyEntries.isEmpty()) {
                float avgNausea = sumNausea / dailyEntries.size();
                float avgFatigue = sumFatigue / dailyEntries.size();
                float avgPain = sumPain / dailyEntries.size();

                weeklyData.put(dateString, new ArrayList<>(Collections.singletonList(new Entry(dateString, (int) avgNausea, (int) avgFatigue, (int) avgPain))));
            }

            // Move to the next day
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            startDate = calendar.getTime();
        }

        return weeklyData;
    }


    private static Map<String, List<Entry>> aggregateDataByMonth(List<Entry> entries) {
        Map<String, List<Entry>> monthlyData = new HashMap<>();
        Calendar calendar = Calendar.getInstance();

        // Iterate over the past 5 weeks before the current date
        for (int i = 0; i < 5; i++) {
            // Set the end date to the start of the week (current date)
            Date endDate = calendar.getTime();

            // Set the start date to 7 days before the current date
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            Date startDate = calendar.getTime();

            String weekStartDate = formatDate(startDate, "yyyy-MM-dd");

            // Collect entries for the current week
            List<Entry> weeklyEntries = new ArrayList<>();
            for (Entry entry : entries) {
                if (entry.isWithinDateRange(startDate, endDate)) {
                    weeklyEntries.add(entry);
                }
            }

            // Calculate average data for the week
            float sumNausea = 0;
            float sumFatigue = 0;
            float sumPain = 0;
            for (Entry entry : weeklyEntries) {
                sumNausea += entry.getNausea();
                sumFatigue += entry.getFatigue();
                sumPain += entry.getPain();
            }

            // Add the averaged data to the monthlyData map
            if (!weeklyEntries.isEmpty()) {
                float avgNausea = sumNausea / weeklyEntries.size();
                float avgFatigue = sumFatigue / weeklyEntries.size();
                float avgPain = sumPain / weeklyEntries.size();

                monthlyData.put(weekStartDate, new ArrayList<>(Collections.singletonList(new Entry(weekStartDate, (int) avgNausea, (int) avgFatigue, (int) avgPain))));
            }
        }

        return monthlyData;
    }


    private static Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String formatDate(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(date);
    }

    private static void plotData(BarChart barChart, Map<String, List<Entry>> data) {
        ArrayList<BarEntry> nauseaEntries = new ArrayList<>();
        ArrayList<BarEntry> fatigueEntries = new ArrayList<>();
        ArrayList<BarEntry> painEntries = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, List<Entry>> entry : data.entrySet()) {
            List<Entry> entries = entry.getValue();

            // Add each entry separately to the corresponding lists with offset for clustering
            for (Entry e : entries) {
                nauseaEntries.add(new BarEntry(index - 0.2f, e.getNausea()));
                fatigueEntries.add(new BarEntry(index, e.getFatigue()));
                painEntries.add(new BarEntry(index + 0.2f, e.getPain()));
                dates.add(entry.getKey());
                index++;
            }
        }

        // Create datasets for each type of data
        BarDataSet setNausea = new BarDataSet(nauseaEntries, "Nausea");
        setNausea.setColor(Color.RED);
        BarDataSet setFatigue = new BarDataSet(fatigueEntries, "Fatigue");
        setFatigue.setColor(Color.BLUE);
        BarDataSet setPain = new BarDataSet(painEntries, "Pain");
        setPain.setColor(Color.GREEN);

        // Combine the datasets
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(setNausea);
        dataSets.add(setFatigue);
        dataSets.add(setPain);

        // Create a BarData object and set the datasets
        BarData barData = new BarData(dataSets);

        // Set the spacing between clusters
        barData.setBarWidth(0.2f);
        barData.groupBars(0, 0.1f, 0.02f); // Adjust the second parameter for cluster spacing
        
        barChart.setData(barData);

        // Set the X-axis labels
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setLabelRotationAngle(45f);
        barChart.getXAxis().setLabelCount(dates.size());
        barChart.getXAxis().setTextSize(10f);

        // Refresh the chart
        barChart.invalidate();
    }


    private static class Entry {
        private final String timestamp;
        private final int nausea;
        private final int fatigue;
        private final int pain;

        public Entry(String timestamp, int nausea, int fatigue, int pain) {
            this.timestamp = timestamp;
            this.nausea = nausea;
            this.fatigue = fatigue;
            this.pain = pain;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public int getNausea() {
            return nausea;
        }

        public int getFatigue() {
            return fatigue;
        }

        public int getPain() {
            return pain;
        }

        public String getFormattedDate(String pattern) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date date = sdf.parse(timestamp);
                SimpleDateFormat outputFormat = new SimpleDateFormat(pattern, Locale.getDefault());
                return outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        public boolean isWithinDateRange(Date startDate, Date endDate) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date entryDate = sdf.parse(timestamp);
                return entryDate.after(startDate) && entryDate.before(endDate);
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public String toString() {
            return "Timestamp: " + timestamp + ", Nausea: " + nausea + ", Fatigue: " + fatigue + ", Pain: " + pain;
        }
    }

    private static class QuanEntry {
        private final String timestamp;
        private final int sensorID;
        private final int data;

        public QuanEntry(String timestamp, int data, int sensorID) {
            this.timestamp = timestamp;
            this.sensorID = sensorID;
            this.data = data;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public int getData() {
            return data;
        }

        public int getSensorID() {
            return sensorID;
        }


        public String getFormattedDate(String pattern) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date date = sdf.parse(timestamp);
                SimpleDateFormat outputFormat = new SimpleDateFormat(pattern, Locale.getDefault());
                return outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        public boolean isWithinDateRange(Date startDate, Date endDate) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date entryDate = sdf.parse(timestamp);
                return entryDate.after(startDate) && entryDate.before(endDate);
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public String toString() {
            if (sensorID == 1) {
                return "Timestamp: " + timestamp + ", HR: " + data;
            }
            else {
                return "Timestamp: " + timestamp + ", Temperature: " + data;
            }

        }
    }
}