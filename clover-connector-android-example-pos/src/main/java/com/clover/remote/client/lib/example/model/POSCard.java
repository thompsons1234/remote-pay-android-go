package com.clover.remote.client.lib.example.model;

/**
 * Created by blakewilliams on 1/12/16.
 */
public class POSCard {
    private String name;
    private String first6;
    private String last4;
    private String month;
    private String year;
    private String token;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirst6() {
        return first6;
    }

    public void setFirst6(String first6) {
        this.first6 = first6;
    }

    public String getLast4() {
        return last4;
    }

    public void setLast4(String last4) {
        this.last4 = last4;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
