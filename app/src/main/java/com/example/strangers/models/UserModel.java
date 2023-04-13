package com.example.strangers.models;

public class UserModel {

    private String userId, name, profile, city;
    private long coins;

    public UserModel() {
    }

    public UserModel(String userId, String name, String profile, String city, long coins) {
        this.userId = userId;
        this.name = name;
        this.profile = profile;
        this.city = city;
        this.coins = coins;
    }

    // Getters and Setters


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getCoins() {
        return coins;
    }

    public void setCoins(long coins) {
        this.coins = coins;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
