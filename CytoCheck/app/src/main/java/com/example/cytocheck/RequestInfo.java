package com.example.cytocheck;

public class RequestInfo {

    private int id;
    private String name;

    public RequestInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
