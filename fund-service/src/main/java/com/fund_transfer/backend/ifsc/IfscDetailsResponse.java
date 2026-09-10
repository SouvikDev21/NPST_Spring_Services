package com.fund_transfer.backend.ifsc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps directly to the Razorpay IFSC API response shape:
 * GET https://ifsc.razorpay.com/{IFSC_CODE}
 *
 * Confirmed live example (SBIN0013913) includes BRANCH, SWIFT, ISO3166,
 * CONTACT, NEFT, DISTRICT, RTGS, UPI, ADDRESS, MICR, STATE, IMPS, CITY,
 * CENTRE, BANK, BANKCODE, IFSC. CONTACT is often an empty string, not null.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IfscDetailsResponse {

    @JsonProperty("IFSC")
    private String ifsc;

    @JsonProperty("BANK")
    private String bankName;

    @JsonProperty("BANKCODE")
    private String bankCode;

    @JsonProperty("BRANCH")
    private String branch;

    @JsonProperty("CENTRE")
    private String centre;

    @JsonProperty("DISTRICT")
    private String district;

    @JsonProperty("ADDRESS")
    private String address;

    @JsonProperty("CITY")
    private String city;

    @JsonProperty("STATE")
    private String state;

    @JsonProperty("ISO3166")
    private String iso3166;

    @JsonProperty("SWIFT")
    private String swift;

    @JsonProperty("MICR")
    private String micr;

    @JsonProperty("CONTACT")
    private String contact;

    @JsonProperty("IMPS")
    private boolean impsEnabled;

    @JsonProperty("NEFT")
    private boolean neftEnabled;

    @JsonProperty("RTGS")
    private boolean rtgsEnabled;

    @JsonProperty("UPI")
    private boolean upiEnabled;

    public IfscDetailsResponse() {
    }

    public String getIfsc() {
        return ifsc;
    }

    public void setIfsc(String ifsc) {
        this.ifsc = ifsc;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCentre() {
        return centre;
    }

    public void setCentre(String centre) {
        this.centre = centre;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getIso3166() {
        return iso3166;
    }

    public void setIso3166(String iso3166) {
        this.iso3166 = iso3166;
    }

    public String getSwift() {
        return swift;
    }

    public void setSwift(String swift) {
        this.swift = swift;
    }

    public String getMicr() {
        return micr;
    }

    public void setMicr(String micr) {
        this.micr = micr;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public boolean isImpsEnabled() {
        return impsEnabled;
    }

    public void setImpsEnabled(boolean impsEnabled) {
        this.impsEnabled = impsEnabled;
    }

    public boolean isNeftEnabled() {
        return neftEnabled;
    }

    public void setNeftEnabled(boolean neftEnabled) {
        this.neftEnabled = neftEnabled;
    }

    public boolean isRtgsEnabled() {
        return rtgsEnabled;
    }

    public void setRtgsEnabled(boolean rtgsEnabled) {
        this.rtgsEnabled = rtgsEnabled;
    }

    public boolean isUpiEnabled() {
        return upiEnabled;
    }

    public void setUpiEnabled(boolean upiEnabled) {
        this.upiEnabled = upiEnabled;
    }
}
