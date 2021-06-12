package com.example.myrun;

import android.Manifest;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.naver.maps.map.NaverMap;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity{


    ArrayList<Long> latList = new ArrayList<Long>();
    ArrayList<Long> lonList = new ArrayList<Long>();
    ArrayList<String> emailList = new ArrayList<String>();
    private static final String TAG = "MapActivity";

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private FusedLocationSource mLocationSource;
    private static NaverMap mNaverMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
    }
}