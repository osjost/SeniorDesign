package com.example.cytocheck;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
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


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DataProcessor {
    private static Map<String, List<QualEntry>> qualDailyData;
    private static Map<String, List<QualEntry>> qualWeeklyData;
    private static Map<String, List<QualEntry>> qualMonthlyData;
    private static boolean qualInit = false;
    private static Map<String, List<QuanEntry>> hrDailyData;
    private static Map<String, List<QuanEntry>> hrWeeklyData;
    private static Map<String, List<QuanEntry>> hrMonthlyData;
    private static boolean hrInit = false;

    private static Map<String, List<QuanEntry>> tempDailyData;
    private static Map<String, List<QuanEntry>> tempWeeklyData;
    private static Map<String, List<QuanEntry>> tempMonthlyData;
    private static boolean tempInit = false;

    public static void setAllToFalse() {
        qualInit = false;
        hrInit = false;
        tempInit = false;
    }

    public static void processData(String jsonData, BarChart barChart, String timeFrame) {
        // This function is called to process the Qualitative data and update the charts
        Log.d("qual status", String.valueOf(qualInit));
        Log.d("hr status", String.valueOf(hrInit));
        Log.d("temp status", String.valueOf(tempInit));

        if (qualInit) {
            switch (timeFrame) {
                case "Weekly":
                    plotData(barChart, qualWeeklyData);
                    break;
                case "Monthly":
                    plotData(barChart, qualMonthlyData);
                    break;
                default:
                    plotData(barChart, qualDailyData);
                    break;
            }
        }
        else {

            List<QualEntry> entries = parseJsonData(jsonData);
            qualDailyData = aggregateDataByDay(entries);
            //plotData(barChart, qualDailyData);
            new Thread(new Runnable() {
                public void run() {
                    qualWeeklyData = aggregateDataByWeek(entries);
                    qualMonthlyData = aggregateDataByMonth(entries);
                }
            }).start();

            qualInit = true;
        }
    }
    public static void processQuanData(String jsonData, LineChart userDaily, BarChart userBar, int sensorID, String timeframe) {
        // This function is called from the patient and provider guis to update the Charts for a specific reading

        if (sensorID == 1) {
            if (hrInit) {
                switch (timeframe) {
                    case "Weekly":
                        userDaily.setVisibility(View.GONE);
                        userBar.setVisibility(View.VISIBLE);
                        plotQuanData(userBar, hrWeeklyData, sensorID);
                        break;
                    case "Monthly":
                        userDaily.setVisibility(View.GONE);
                        userBar.setVisibility(View.VISIBLE);
                        plotQuanData(userBar, hrMonthlyData, sensorID);
                        break;
                    default:
                        userDaily.setVisibility(View.VISIBLE);
                        userBar.setVisibility(View.GONE);
                        plotQuanLine(userDaily, hrDailyData, sensorID);
                        break;
                }
            }
            else {

                List<QuanEntry> entries = parseJsonQuanData(jsonData, sensorID);
                //userDaily.setVisibility(View.VISIBLE);
                //userBar.setVisibility(View.GONE);
                hrDailyData = aggregateQuanDataByDay(entries, sensorID);
                //plotQuanLine(userDaily, hrDailyData, sensorID);
                new Thread(new Runnable() {
                    public void run() {
                        hrWeeklyData = aggregateQuanDataByWeek(entries, sensorID);
                        hrMonthlyData = aggregateQuanDataByMonth(entries, sensorID);
                    }
                }).start();

                hrInit = true;
            }
        }
        else {
            if (tempInit) {
                switch (timeframe) {
                    case "Weekly":
                        userDaily.setVisibility(View.GONE);
                        userBar.setVisibility(View.VISIBLE);
                        plotQuanData(userBar, tempWeeklyData, sensorID);
                        break;
                    case "Monthly":
                        userDaily.setVisibility(View.GONE);
                        userBar.setVisibility(View.VISIBLE);
                        plotQuanData(userBar, tempMonthlyData, sensorID);
                        break;
                    default:
                        userDaily.setVisibility(View.VISIBLE);
                        userBar.setVisibility(View.GONE);
                        plotQuanLine(userDaily, tempDailyData, sensorID);
                        break;
                }
            }
            else {

                List<QuanEntry> entries = parseJsonQuanData(jsonData, sensorID);
                tempDailyData = aggregateQuanDataByDay(entries, sensorID);
                //userDaily.setVisibility(View.VISIBLE);
                //userBar.setVisibility(View.GONE);
                //plotQuanLine(userDaily, tempDailyData, sensorID);
                new Thread(new Runnable() {
                    public void run() {
                        tempWeeklyData = aggregateQuanDataByWeek(entries, sensorID);
                        tempMonthlyData = aggregateQuanDataByMonth(entries, sensorID);
                    }
                }).start();

                tempInit = true;
            }
        }
    }

    private static List<QualEntry> parseJsonData(String jsonData) {
        // This parses the Qualitative data into a list for further use
        List<QualEntry> entries = new ArrayList<>();
        if (jsonData == null || jsonData == "[]" || jsonData == "") {
            return entries;
        }
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int nausea = jsonObject.getInt("nausea");
                int fatigue = jsonObject.getInt("fatigue");
                int pain = jsonObject.getInt("pain");
                String timestamp = jsonObject.getString("time_stamp");

                // Convert timestamp to local time
                String localTimestamp = convertToLocale(timestamp);

                entries.add(new QualEntry(localTimestamp, nausea, fatigue, pain));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return entries;
    }

    private static List<QuanEntry> parseJsonQuanData(String jsonData, int sensorID) {
        // This function goes through the server response and parses the data into a list
        List<QuanEntry> entries = new ArrayList<>();
        if (jsonData == null || jsonData == "[]" || jsonData == "") {
            return entries;
        }
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has("day")) {
                    String day = jsonObject.getString("day");
                    String timeStamp = convertToLocale(day);
                    double averageReading = jsonObject.getDouble("average_reading");
                    entries.add(new QuanEntry(timeStamp, averageReading, sensorID));
                } else {
                    double data = jsonObject.getDouble("reading");
                    String timestamp = jsonObject.getString("time_stamp");
                    String localTimestamp = convertToLocale(timestamp);
                    entries.add(new QuanEntry(localTimestamp, data, sensorID));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return entries;
    }

    private static String convertToLocale(String timestamp) {
        // This function is used to convert the server time to a phone's local time zone to ensure the correct data is obtained
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

            // Parse the timestamp using England time zone (UTC/GMT)
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(timestamp);

            // Convert to local time zone
            sdf.setTimeZone(TimeZone.getDefault()); // Get the local time zone of the device
            return sdf.format(date); // Return the formatted local timestamp
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    /*  The following are data aggregation functions. There are 3 functions for the quantitative
    * data and 3 functions for the qualitative data. These are used to process the list entries
    * of data into the correct data sets that we use for graphing. This takes a long time combined
    * with parsing the json objects so we try to do it in the background */
    private static Map<String, List<QualEntry>> aggregateDataByDay(List<QualEntry> entries) {
        Map<String, List<QualEntry>> dailyData = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(calendar.getTime()); // Get the current date

        for (QualEntry entry : entries) {
            String date = entry.getFormattedDate("yyyy-MM-dd");
            if (date.equals(currentDate)) {
                // Check if the date already exists in the map
                date = entry.getFormattedDate("MM-dd");
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
                date = entry.getFormattedDate("MM-dd");
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
                dateString = formatDate(calendar.getTime(), "MM-dd");
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
            String dateString = formatDate(calendar.getTime(), "MM-dd");
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
            String endDateString = formatDate(calendar.getTime(), "MM-dd");
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
            String startDateString = formatDate(calendar.getTime(), "MM-dd");
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
            String endDateString = formatDate(calendar.getTime(), "MM-dd");
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
            String startDateString = formatDate(calendar.getTime(), "MM-dd");
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
        // This is used when formatting dates for objects that aren't entries
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(date);
    }

    private static void plotData(BarChart barChart, Map<String, List<QualEntry>> data) {
        // This plots Qualitative data to a given bar chart for all daily weekly and monthly views
        ArrayList<BarEntry> nauseaEntries = new ArrayList<>();
        ArrayList<BarEntry> fatigueEntries = new ArrayList<>();
        ArrayList<BarEntry> painEntries = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        int index = 0;
        if (data == null) {
            barChart.setVisibility(View.GONE);
            return;
        }
        else {
            barChart.setVisibility(View.VISIBLE);
        }

        for (Map.Entry<String, List<QualEntry>> entry : data.entrySet()) {
            List<QualEntry> entries = entry.getValue();

            // Add each entry separately to the corresponding lists with offset for clustering
            for (QualEntry e : entries) {
                nauseaEntries.add(new BarEntry(index - 0.2f, e.getNausea()));
                fatigueEntries.add(new BarEntry(index, e.getFatigue()));
                painEntries.add(new BarEntry(index + 0.2f, e.getPain()));
                dates.add(entry.getKey().substring(5));
                index++;
            }
        }

        // Create datasets for each type of data
        BarDataSet setNausea = new BarDataSet(nauseaEntries, "Nausea");
        setNausea.setColor(Color.rgb(0,141,255));
        BarDataSet setFatigue = new BarDataSet(fatigueEntries, "Fatigue");
        setFatigue.setColor(Color.rgb(4,88,155));
        BarDataSet setPain = new BarDataSet(painEntries, "Pain");
        setPain.setColor(Color.rgb(0,61,110));

        // Combine the datasets
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(setNausea);
        dataSets.add(setFatigue);
        dataSets.add(setPain);

        // Create a BarData object and set the datasets
        BarData barData = new BarData(dataSets);
        barData.setDrawValues(false);

        // Set the spacing between clusters
        barData.setBarWidth(0.2f);
        barData.groupBars(0, 0.1f, 0.02f); // Adjust the second parameter for cluster spacing
        
        barChart.setData(barData);

        barChart.getDescription().setEnabled(false); // Disable description
        barChart.setDrawGridBackground(false); // Disable grid background


        // Set the X-axis labels
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setLabelRotationAngle(45f);
        barChart.getXAxis().setLabelCount(dates.size());
        barChart.getXAxis().setTextSize(10f);

        // Set the Y-axis
        barChart.getAxisLeft().setAxisMaximum(10);
        barChart.getAxisLeft().setAxisMinimum(0);

        // Refresh the chart
        barChart.invalidate();
    }

    private static void plotQuanData(BarChart barChart, Map<String, List<QuanEntry>> data, int sensorID) {
        //This function plots quantitative data to a given bar chart for monthly and weekly views
        ArrayList<BarEntry> dataEntries = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        int index = 0;
        if (data == null) {
            barChart.setVisibility(View.GONE);
            return;
        }
        else {
            barChart.setVisibility(View.VISIBLE);
        }
        for (Map.Entry<String, List<QuanEntry>> entry : data.entrySet()) {
            List<QuanEntry> entries = entry.getValue();

            // Add each entry separately to the corresponding lists with offset for clustering
            for (QuanEntry e : entries) {
                float xPos = index;
                dataEntries.add(new BarEntry(xPos, (float) e.getData()));
                dates.add(entry.getKey().substring(5));
                index++;
            }

        }

        // Create a BarData object and set the dataset
        BarDataSet dataSet = new BarDataSet(dataEntries, "Sensor Data");
        dataSet.setDrawValues(false);
        if (sensorID == 1) {
            dataSet.setLabel("Heart Rate");
            dataSet.setColor(Color.rgb(242,105,32)); // Set the bar color
        }
        else {
            dataSet.setLabel("Temperature");
            dataSet.setColor(Color.rgb(4,88,155)); // Set the bar color
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
        // This function Plots quantitative data to a given linechart for the daily data
        ArrayList<Entry> lineEntries = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        if (data == null) {
            lineChart.setVisibility(View.GONE);
            return;
        }
        else {
            lineChart.setVisibility(View.VISIBLE);
        }

        for (Map.Entry<String, List<QuanEntry>> entry : data.entrySet()) {
            List<QuanEntry> entries = entry.getValue();
            for (QuanEntry e : entries) {
                lineEntries.add(new Entry( lineEntries.size(), (float) e.getData()));
            }



        }

        // Create a dataset for the line chart
        LineDataSet dataSet = new LineDataSet(lineEntries, "Average Data");
        dataSet.setDrawValues(false);
        dataSet.setColor(Color.BLUE); // Set the line color
        if (sensorID == 1) {
            dataSet.setLabel("Heart Rate");
            dataSet.setColor(Color.rgb(242,105,32)); // Set the line color
            dataSet.setCircleColor(Color.rgb(242,105,32));
            //        Set the Y-axis interval
            lineChart.getAxisLeft().setAxisMinimum(30);
            lineChart.getAxisLeft().setAxisMaximum(200);
        } else {
            dataSet.setLabel("Temperature");
            dataSet.setColor(Color.rgb(4,88,155)); // Set the line color
            dataSet.setCircleColor(Color.rgb(4,88,155));
            //        Set the Y-axis interval
            lineChart.getAxisLeft().setAxisMinimum(90);
            lineChart.getAxisLeft().setAxisMaximum(115);
        }


        // Combine the datasets
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        // Create a LineData object and set the dataset
        LineData lineData = new LineData(dataSets);

        // Set the data to the line chart
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);

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

        //Constructor for the Qualitative entry object for pain, fatigue and nausea
        public QualEntry(String timestamp, int nausea, int fatigue, int pain) {
            this.timestamp = timestamp;
            this.nausea = nausea;
            this.fatigue = fatigue;
            this.pain = pain;
        }

        //Getter Methods
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
            // Formats the timestamp of an entry based on a pattern
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
            // Checks if the timestamp for an entry is within the given date
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

        @Override
        public String toString() {
            // Used for debugging and logging Qualitative data
            return "Timestamp: " + timestamp + ", Nausea: " + nausea + ", Fatigue: " + fatigue + ", Pain: " + pain;
        }
    }

    private static class QuanEntry {
        private final String timestamp;
        private final int sensorID;
        private final double data;

        // Constructor for the Quantitative entry for HeartRate and Temperature
        public QuanEntry(String timestamp, double data, int sensorID) {
            this.timestamp = timestamp;
            this.sensorID = sensorID;
            this.data = data;
        }

        // Getter Methods
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
            // Formats an Entry's date based off of the timestamp and a pattern
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
            // Uses an Entry's timestamp to see if it is within the given day
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

            @Override
        public String toString() {
            // Used for Logging and Debugging
            if (sensorID == 1) {
                return "Timestamp: " + timestamp + ", HR: " + data;
            }
            else {
                return "Timestamp: " + timestamp + ", Temperature: " + data;
            }
        }
    }
}