package com.abeed.mesme.Models;

public class User {

    private String uid;
    private String name;
    private String phoneNumber;
    private String profileImage;
    private String about;
    private String token;
    private Long msgs;

    public User() {

    }

    public User(String uid, String name, String phoneNumber, String profileImage, String about, Long msgs) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
        this.about = about;
        this.msgs = msgs;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getMsgs() {
        return msgs;
    }

    public void setMsgs(Long msgs) {
        this.msgs = msgs;
    }
}
