package com.example.myrun.model;

import com.google.firebase.Timestamp;

public class User {
    private String userEmail; // 유저 이메일
    private String userNickName; // 유저 닉네임
    private Timestamp registerTime; // 가입한 시간

    public User(String userEmail, String userNickName, Timestamp registerTime){
        this.userEmail = userEmail;
        this.userNickName = userNickName;
        this.registerTime = registerTime;
    }

    public String getUserNickName() {
        return userNickName;
    }
}
