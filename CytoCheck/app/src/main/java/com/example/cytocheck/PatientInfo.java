package com.example.cytocheck;

public class PatientInfo {
    private int id;
    private String name;
    private String phone;
    private String email;
    private String dob;
    private String qualData;
    private String hrData;
    private String tempData;

    public PatientInfo(int id, String name, String phone, String email, String dob) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.dob = dob;
        this.qualData = "";
        this.hrData = "";
        this.tempData = "";
    }

    protected int getId() {
        return id;
    }

    protected String getName() {
        return name;
    }

    protected String getEmail() {
        return email;
    }
    protected String getPhone() {
        return phone;
    }

    protected void setQualData(String qData) {
        this.qualData = qData;
    }
    protected void setHRData(String hData) {
        this.hrData = hData;
    }
    protected void setTempData(String tData) {
        this.tempData = tData;
    }
    protected String getQualData() {
        return this.qualData;
    }
    protected String getHRData() {
        return this.hrData;
    }
    protected String getTempData() {
        return this.tempData;
    }
}