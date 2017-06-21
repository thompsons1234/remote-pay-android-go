package com.clover.remote.client.clovergo.messages;

/**
 * Created by Akhani, Avdhesh on 6/19/17.
 */

public class BillingAddress {
    private String street;
    private String zipPostalCode;
    private String country;

    public BillingAddress(String street, String zipPostalCode, String country) {
        this.street = street;
        this.zipPostalCode = zipPostalCode;
        this.country = country;
    }

    public String getStreet() {
        return this.street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipPostalCode() {
        return this.zipPostalCode;
    }

    public void setZipPostalCode(String zipPostalCode) {
        this.zipPostalCode = zipPostalCode;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

}
