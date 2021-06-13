package com.example.myrun;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.round;


public class GamingActivity extends AppCompatActivity implements OnMapReadyCallback {

    Toolbar toolbar;
    PathOverlay path = new PathOverlay();
    ArrayList<LatLng> locationList = new ArrayList<>();
    SoundPool mPool;
    int mDdok;
    ArrayList<Marker> markers = new ArrayList<>();

    double totald;

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

        mPool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
        mDdok = mPool.load(this, R.raw.ddok,1);

        toolbar = findViewById(R.id.toolbar_map);
        //상단 툴바 설정
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true); // 커스터마이징 하기 위해 필요
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#55e6c3"))); // 툴바 배경색


        // 위치를 반환하는 구현체인 FusedLocationSource 생성
        mLocationSource =
                new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

        // 지도 객체 생성
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }

        // getMapAsync를 호출하여 비동기로 onMapReady 콜백 메서드 호출
        // onMapReady에서 NaverMap 객체를 받음
        mapFragment.getMapAsync(this);

        TextView km = findViewById(R.id.km);
        TextView time = findViewById(R.id.Ntime);
        TextView kc = findViewById(R.id.Kc);

        final long startTime = System.currentTimeMillis();

        Button btnstart = findViewById(R.id.btnNormalstart);
        MapFragment finalMapFragment = mapFragment;
        btnstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocationSource.getLastLocation()==null) {

                } else {
                    double lat = mLocationSource.getLastLocation().getLatitude();
                    double lon = mLocationSource.getLastLocation().getLongitude();
                    locationList.add(new LatLng(lat,lon));
                    if (locationList.size() > 2) {
                        double lat1 = locationList.get(locationList.size()-2).latitude;
                        double lon1 = locationList.get(locationList.size()-2).longitude;
                        totald = totald + Math.sqrt(Math.pow(lon-lon1,2)+Math.pow(lat-lat1,2));
                        long endTime = System.currentTimeMillis();
                        time.setText(Long.toString((endTime - startTime)/1000) + " second");
                        km.setText(Double.toString(round(totald*100000)/1000.0)+" km");
                        kc.setText(Double.toString(round(totald*6000000)/1000.0) + " Kcal");
                        Double tempkm = (round(totald*100000)/1000.0) / ((endTime - startTime)/1000);
                        long tempTime = endTime - startTime;
                        tempTime = tempTime/1000;
                        if (round(Math.sqrt(Math.pow(lon-lon1,2)+Math.pow(lat-lat1,2))*100000)/1000.0 < tempkm && tempTime > 20) {
                            mPool.play(mDdok,1,1,0,1,1);
                            Toast.makeText(getApplicationContext(),"문어아빠가 다가옵니다!",Toast.LENGTH_SHORT).show();
                        }
                        path.setCoords(locationList);
                        finalMapFragment.getMapAsync(GamingActivity.this);
                    }
                }
            }
        });

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                GamingActivity.this.runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        btnstart.performClick();
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 5000, 2000);

        Button btnstop = findViewById(R.id.btnNormalStop);
        btnstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                Intent intent = new Intent(GamingActivity.this, GamingEnd.class);
                intent.putExtra("key", 1);
                intent.putExtra("km",km.getText().toString());
                intent.putExtra("time",time.getText().toString());
                intent.putExtra("kc",kc.getText().toString());
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Log.d( TAG, "onMapReady");

        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        mNaverMap = naverMap;
        mNaverMap.setMapType(NaverMap.MapType.Basic);
        if (locationList.size() > 2) {
            try {
                markers.get(0).setMap(null);
                markers.clear();
            } catch (Exception e) {

            }
            path.setColor(Color.GREEN);
            path.setMap(mNaverMap);
            Marker marker = new Marker();
            markers.add(marker);
            marker.setIcon(MarkerIcons.BLACK);
            marker.setIconTintColor(Color.RED);
            marker.setPosition(locationList.get(locationList.size()-2));
            marker.setMap(mNaverMap);
        }
        mNaverMap.setLocationSource(mLocationSource);
        mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        // 권한확인. 결과는 onRequestPermissionsResult 콜백 매서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // request code와 권한획득 여부 확인
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }
    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
