package com.example.cytocheck;

public class PatientInfo {
    private int id;
    private String name;
    private String qualData;
    private String hrData;
    private String tempData;

    public PatientInfo(int id, String name) {
        this.id = id;
        this.name = name;
        this.qualData = "";
        this.hrData = "";
        this.tempData = "";
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setQualData(String qData) {
        this.qualData = qData;
    }
    public void setHRData(String hData) {
        this.hrData = hData;
    }
    public void setTempData(String tData) {
        this.tempData = tData;
    }
    public String getQualData() {
        return this.qualData;
    }
    public String getHRData() {
        return this.hrData;
    }
    public String getTempData() {
        return this.tempData;
    }
}