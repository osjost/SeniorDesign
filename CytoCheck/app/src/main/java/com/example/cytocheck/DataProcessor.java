package com.example.cytocheck;

import android.graphics.Color;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;




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
        List<QualEntry> entries = parseJsonData(jsonData);
        Map<String, List<QualEntry>> data;

        switch (timeFrame) {
            case "Weekly":
                data = aggregateDataByWeek(entries);
                break;
            case "Monthly":
                data = aggregateDataByMonth(entries);
                break;
            default:
                data = aggregateDataByDay(entries);
                break;
        }

        plotData(barChart, data);
    }
    public static void processQuanData(String jsonData, LineChart userDaily, BarChart userBar, int sensorID, String timeframe) {
        List<QuanEntry> entries = parseJsonQuanData(jsonData);
        Map<String, List<QuanEntry>> data;

        switch (timeframe) {
            case "Weekly":
                userDaily.setVisibility(View.GONE);
                userBar.setVisibility(View.VISIBLE);
                data = aggregateQuanDataByWeek(entries, sensorID);
                plotQuanData(userBar, data, sensorID);
                break;
            case "Monthly":
                userDaily.setVisibility(View.GONE);
                userBar.setVisibility(View.VISIBLE);
                data = aggregateQuanDataByMonth(entries, sensorID);
                plotQuanData(userBar, data, sensorID);
                break;
            default:
                userDaily.setVisibility(View.VISIBLE);
                userBar.setVisibility(View.GONE);
                data = aggregateQuanDataByDay(entries, sensorID);
                plotQuanLine(userDaily, data, sensorID);
                break;
        }
    }

    private static List<QualEntry> parseJsonData(String jsonData) {
        List<QualEntry> entries = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int nausea = jsonObject.getInt("nausea");
                int fatigue = jsonObject.getInt("fatigue");
                int pain = jsonObject.getInt("pain");
                String timestamp = jsonObject.getString("time_stamp");

                entries.add(new QualEntry(timestamp, nausea, fatigue, pain));
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
                double data = jsonObject.getDouble("reading");
                int sensorID = jsonObject.getInt("sensor_id");
                String timestamp = jsonObject.getString("time_stamp");

                entries.add(new QuanEntry(timestamp, data, sensorID));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return entries;
    }

    private static Map<String, List<QualEntry>> aggregateDataByDay(List<QualEntry> entries) {
        Map<String, List<QualEntry>> dailyData = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(calendar.getTime()); // Get the current date

        for (QualEntry entry : entries) {
            String date = entry.getFormattedDate("yyyy-MM-dd");
            if (date.equals(currentDate)) {
                // Check if the date already exists in the map
                if (!dailyData.containsKey(date)) {
                    dailyData.put(date, new ArrayList<>());
                }

                // Check if the entry already exists in the list for the current date
                if (!dailyData.get(date).contains(entry)) {
                    dailyData.get(date).add(entry);
                }
            }
        }
        return dailyData;
    }

    private static Map<String, List<QuanEntry>> aggregateQuanDataByDay(List<QuanEntry> entries, int sensorID) {
        Map<String, List<QuanEntry>> dailyData = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(calendar.getTime()); // Get the current date

        for (QuanEntry entry : entries) {
            String date = entry.getFormattedDate("yyyy-MM-dd");
            if (date.equals(currentDate)) {
                // Check if the date already exists in the map
                if (!dailyData.containsKey(date)) {
                    List<QuanEntry> entryList = new ArrayList<>();
                    entryList.add(new QuanEntry(entry.getTimestamp(), entry.getData(), sensorID));
                    dailyData.put(date, entryList);
                } else {
                    // Add the entry to the existing list for that date
                    dailyData.get(date).add(new QuanEntry(entry.getTimestamp(), entry.getData(), sensorID));
                }
            }
        }
        return dailyData;
    }




    private static Map<String, List<QualEntry>> aggregateDataByWeek(List<QualEntry> entries) {
        Map<String, List<QualEntry>> weeklyData = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        // Set the end date to today
        Date endDate = calendar.getTime();

        // Set the start date to 7 days before today
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date startDate = calendar.getTime();

        // Iterate over the entries and aggregate data for each day
        while (!endDate.before(startDate)) {
            String dateString = formatDate(calendar.getTime(), "yyyy-MM-dd");
            List<QualEntry> dailyEntries = new ArrayList<>();

            // Collect entries for the current day
            for (QualEntry entry : entries) {
                if (entry.isWithinDateRange(startDate)) {
                    dailyEntries.add(entry);
                }
            }

            // Calculate average data for the day
            float sumNausea = 0;
            float sumFatigue = 0;
            float sumPain = 0;
            for (QualEntry entry : dailyEntries) {
                sumNausea += entry.getNausea();
                sumFatigue += entry.getFatigue();
                sumPain += entry.getPain();
            }

            // Add the averaged data to the weeklyData map
            if (!dailyEntries.isEmpty()) {
                float avgNausea = sumNausea / dailyEntries.size();
                float avgFatigue = sumFatigue / dailyEntries.size();
                float avgPain = sumPain / dailyEntries.size();

                weeklyData.put(dateString, new ArrayList<>(Collections.singletonList(new QualEntry(dateString, (int) avgNausea, (int) avgFatigue, (int) avgPain))));
            }

            // Move to the next day
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            startDate = calendar.getTime();
        }

        return weeklyData;
    }


    private static Map<String, List<QuanEntry>> aggregateQuanDataByWeek(List<QuanEntry> entries, int sensorID) {
        Map<String, List<QuanEntry>> weeklyData = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        // Set the end date to today
        Date endDate = calendar.getTime();

        // Set the start date to 7 days before today
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date startDate = calendar.getTime();

        // Iterate over the entries and aggregate data for each day
        while (!endDate.before(startDate)) {
            String dateString = formatDate(calendar.getTime(), "yyyy-MM-dd");
            List<QuanEntry> dailyEntries = new ArrayList<>();

            // Collect entries for the current day and sensor ID
            for (QuanEntry entry : entries) {
                if (entry.isWithinDateRange(startDate) && entry.getSensorID() == sensorID) {
                    dailyEntries.add(entry);
                }
            }

            // Calculate average data for the day
            float sumData = 0;
            for (QuanEntry entry : dailyEntries) {
                sumData += entry.getData();
            }

            // Add the daily entries to the weeklyData map
            if (!dailyEntries.isEmpty()) {
                float avgData = sumData / dailyEntries.size();

                // Check if the date already exists in the map
                if (!weeklyData.containsKey(dateString)) {
                    weeklyData.put(dateString, new ArrayList<>());
                }

                // Add the averaged data entry to the list for that date
                weeklyData.get(dateString).add(new QuanEntry(dateString, avgData, sensorID));
            }

            // Move to the next day
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            startDate = calendar.getTime();
        }

        return weeklyData;
    }




    private static Map<String, List<QualEntry>> aggregateDataByMonth(List<QualEntry> entries) {
        Map<String, List<QualEntry>> monthlyData = new HashMap<>();
        Calendar calendar = Calendar.getInstance();

        // Iterate over the past 5 weeks before the current date
        for (int i = 0; i < 5; i++) {
            // i is equal to the week number -1
            String endDateString = formatDate(calendar.getTime(), "yyyy-MM-dd");
            Calendar endDate = calendar;
            List weeklyEntries = new ArrayList<>();
            float sumNausea = 0;
            float sumPain = 0;
            float sumFatigue = 0;
            for (int j = 0; j < 7; j++) {
                // j is the day in the week - 1
                for (QualEntry e : entries) {
                    if (e.isWithinDateRange(calendar.getTime())) {
                        sumNausea += e.getNausea();
                        sumFatigue += e.getFatigue();
                        sumPain += e.getPain();
                        weeklyEntries.add(e);
                    }
                }

                calendar.add(Calendar.DAY_OF_YEAR, -1);
            }
            String startDateString = formatDate(calendar.getTime(), "yyyy-MM-dd");
            if (!weeklyEntries.isEmpty()) {
                float avgNausea = sumNausea / weeklyEntries.size();
                float avgFatigue = sumFatigue / weeklyEntries.size();
                float avgPain = sumPain / weeklyEntries.size();

                monthlyData.put(startDateString + " to " + endDateString, new ArrayList<>(Collections.singletonList(new QualEntry(startDateString + " to " + endDateString, (int) avgNausea, (int) avgFatigue, (int) avgPain))));
            }
        }

        return monthlyData;
    }

    private static Map<String, List<QuanEntry>> aggregateQuanDataByMonth(List<QuanEntry> entries, int sensorID) {
        Map<String, List<QuanEntry>> monthlyData = new HashMap<>();
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 5; i++) {
            // i is equal to the week number -1
            String endDateString = formatDate(calendar.getTime(), "yyyy-MM-dd");
            Calendar endDate = calendar;
            List weeklyEntries = new ArrayList<>();
            float sumData = 0;
            for (int j = 0; j < 7; j++) {
                // j is the day in the week - 1
                for (QuanEntry e : entries) {
                    if (e.isWithinDateRange(calendar.getTime()) && e.getSensorID() == sensorID) {
                        sumData += e.getData();
                        weeklyEntries.add(e);
                    }
                }

                calendar.add(Calendar.DAY_OF_YEAR, -1);
            }
            String startDateString = formatDate(calendar.getTime(), "yyyy-MM-dd");
            if (!weeklyEntries.isEmpty()) {
                float avgData = sumData /weeklyEntries.size();
                if (!monthlyData.containsKey(startDateString + " to " + endDateString)) {
                    monthlyData.put(startDateString + " to " + endDateString, new ArrayList<>());
                }
                monthlyData.get(startDateString + " to " + endDateString).add(new QuanEntry(startDateString + " to " + endDateString,avgData,sensorID));
            }
        }

        return monthlyData;
    }



    private static String formatDate(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(date);
    }

    private static void plotData(BarChart barChart, Map<String, List<QualEntry>> data) {
        ArrayList<BarEntry> nauseaEntries = new ArrayList<>();
        ArrayList<BarEntry> fatigueEntries = new ArrayList<>();
        ArrayList<BarEntry> painEntries = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, List<QualEntry>> entry : data.entrySet()) {
            List<QualEntry> entries = entry.getValue();

            // Add each entry separately to the corresponding lists with offset for clustering
            for (QualEntry e : entries) {
                nauseaEntries.add(new BarEntry(index - 0.2f, e.getNausea()));
                fatigueEntries.add(new BarEntry(index, e.getFatigue()));
                painEntries.add(new BarEntry(index + 0.2f, e.getPain()));
                dates.add(entry.getKey());
                index++;
            }
        }

        // Create datasets for each type of data
        BarDataSet setNausea = new BarDataSet(nauseaEntries, "Nausea");
        setNausea.setColor(Color.GREEN);
        BarDataSet setFatigue = new BarDataSet(fatigueEntries, "Fatigue");
        setFatigue.setColor(Color.YELLOW);
        BarDataSet setPain = new BarDataSet(painEntries, "Pain");
        setPain.setColor(Color.RED);

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

    private static void plotQuanData(BarChart barChart, Map<String, List<QuanEntry>> data, int sensorID) {
        ArrayList<BarEntry> dataEntries = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        int index = 0;

        for (Map.Entry<String, List<QuanEntry>> entry : data.entrySet()) {
            List<QuanEntry> entries = entry.getValue();

            // Add each entry separately to the corresponding lists with offset for clustering
            for (QuanEntry e : entries) {
                float xPos = index;
                dataEntries.add(new BarEntry(xPos, (float) e.getData()));
                dates.add(entry.getKey());
                index++;
            }

        }

        // Create a BarData object and set the dataset
        BarDataSet dataSet = new BarDataSet(dataEntries, "Sensor Data");
        if (sensorID == 1) {
            dataSet.setLabel("Heart Rate");
            dataSet.setColor(Color.RED); // Set the bar color
        }
        else {
            dataSet.setLabel("Temperature");
            dataSet.setColor(Color.BLUE); // Set the bar color
        }


        // Combine the dataset into an ArrayList
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        // Create a BarData object with the combined dataset
        BarData barData = new BarData(dataSets);

        // Customize the BarChart appearance
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false); // Disable description
        barChart.setDrawGridBackground(false); // Disable grid background
        barChart.animateY(1000); // Add animation

        // Set the X-axis labels and other properties
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates)); // Set the date labels
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Position the labels at the bottom
        xAxis.setGranularity(1f); // Set the label granularity
        xAxis.setLabelRotationAngle(45f); // Rotate the labels for better readability
        xAxis.setLabelCount(dates.size()); // Set the label count
        xAxis.setTextSize(10f); // Set the label text size

        // Refresh the chart
        barChart.invalidate();
    }




    private static void plotQuanLine(LineChart lineChart, Map<String, List<QuanEntry>> data, int sensorID) {
        ArrayList<Entry> lineEntries = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        for (Map.Entry<String, List<QuanEntry>> entry : data.entrySet()) {
            List<QuanEntry> entries = entry.getValue();
            for (QuanEntry e : entries) {
                lineEntries.add(new Entry( lineEntries.size(), (float) e.getData()));
            }



        }

        // Create a dataset for the line chart
        LineDataSet dataSet = new LineDataSet(lineEntries, "Average Data");
        if (sensorID == 1) {
            dataSet.setLabel("Heart Rate");
        } else {
            dataSet.setLabel("Temperature");
        }
        dataSet.setColor(Color.BLUE); // Set the line color
        dataSet.setCircleColor(Color.BLUE); // Set the circle color for data points

        // Combine the datasets
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        // Create a LineData object and set the dataset
        LineData lineData = new LineData(dataSets);

        // Set the data to the line chart
        lineChart.setData(lineData);

        // Set the X-axis labels
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setLabelRotationAngle(45f);
        lineChart.getXAxis().setLabelCount(dates.size());
        lineChart.getXAxis().setTextSize(10f);

        // Refresh the chart
        lineChart.invalidate();
    }





    private static class QualEntry {
        private final String timestamp;
        private final int nausea;
        private final int fatigue;
        private final int pain;

        public QualEntry(String timestamp, int nausea, int fatigue, int pain) {
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

        public boolean isWithinDateRange(Date startDate) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date entryDate = sdf.parse(timestamp);

                // Create separate date formats for comparing day elements only
                SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String entryDay = dayFormat.format(entryDate);
                String startDay = dayFormat.format(startDate);

                return entryDay.equals(startDay); // Check if day elements are equal
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }
        public boolean isWithinWeek(Date startDate, Date endDate) {
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
        private final double data;

        public QuanEntry(String timestamp, double data, int sensorID) {
            this.timestamp = timestamp;
            this.sensorID = sensorID;
            this.data = data;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public double getData() {
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

        public boolean isWithinDateRange(Date startDate) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date entryDate = sdf.parse(timestamp);

                // Create separate date formats for comparing day elements only
                SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String entryDay = dayFormat.format(entryDate);
                String startDay = dayFormat.format(startDate);

                return entryDay.equals(startDay); // Check if day elements are equal
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }
        public boolean isWithinWeek(Date startDate, Date endDate) {
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