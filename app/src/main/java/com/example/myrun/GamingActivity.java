package com.example.myrun;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
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
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.round;


public class GamingActivity extends AppCompatActivity implements OnMapReadyCallback {
    /*
    전역변수 선언
    툴바, 지도 객체와 요소들, 권한코드
    사운드관련 변수
     */
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

        // 사운드 관련 변수 할당
        mPool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
        mDdok = mPool.load(this, R.raw.ddok,1);

        //상단 툴바 설정
        toolbar = findViewById(R.id.toolbar_map);
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

        // 거리 시간 칼로리 연결
        TextView km = findViewById(R.id.km);
        TextView time = findViewById(R.id.Ntime);
        TextView kc = findViewById(R.id.Kc);

        // 러닝 시작 시간
        final long startTime = System.currentTimeMillis();

        // start 버튼 ( 동기화 기능을 함, 스케줄링으로 자동 클릭 )
        Button btnstart = findViewById(R.id.btnNormalstart);

        // 로컬을 위한 MapFragment 선언
        MapFragment finalMapFragment = mapFragment;
        btnstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocationSource.getLastLocation()==null) {
                    // 현재 위치가 잡히지 않으면 pass
                } else {
                    // 현재 위치가 잡힐 경우
                    double lat = mLocationSource.getLastLocation().getLatitude(); // 위도
                    double lon = mLocationSource.getLastLocation().getLongitude(); // 경도
                    locationList.add(new LatLng(lat,lon)); // 위도 경도를 리스트에 저장
                    if (locationList.size() > 2) { // 리스트의 길이가 3 이상일 경우 ( 경로 표시를 위한 최소 개수 )
                        // 현재에서 바로 직전의 위치를 가져옴
                        double lat1 = locationList.get(locationList.size()-2).latitude; // 위도
                        double lon1 = locationList.get(locationList.size()-2).longitude; // 경도

                        // 현재 위치와 직전 위치의 좌표 차이로 직선 거리 계산 후 총 거리에 계속 더해줌
                        totald = totald + Math.sqrt(Math.pow(lon-lon1,2)+Math.pow(lat-lat1,2));
                        // 현재 시간 불러옴
                        long endTime = System.currentTimeMillis();
                        // 표시할 시간 = (현재시간 - 러닝 시작 시간) * 1000
                        time.setText(Long.toString((endTime - startTime)/1000) + " second");
                        // 달린 거리 = 총 거리 * 100 (소수점 셋째에서 반올림)
                        km.setText(Double.toString(round(totald*100000)/1000.0)+" km");
                        // 총 칼로리 = 달린거리 * 60
                        kc.setText(Double.toString(round(totald*6000000)/1000.0) + " Kcal");
                        // 평균 이동 거리
                        Double tempkm = (round(totald*100000)/1000.0) / ((endTime - startTime)/1000);
                        // 현재 소요 시간
                        long tempTime = endTime - startTime;
                        tempTime = tempTime/1000;
                        // 직전의 이동거리가 평균 이동거리보다 작고, 현재 소요시간이 20초 이상일 경우
                        if (round(Math.sqrt(Math.pow(lon-lon1,2)+Math.pow(lat-lat1,2))*100000)/1000.0 < tempkm && tempTime > 20) {
                            // 소리가 나고 메세지 출력
                            mPool.play(mDdok,1,1,0,1,1);
                            Toast.makeText(getApplicationContext(),"문어아빠가 다가옵니다!",Toast.LENGTH_SHORT).show();
                        }
                        // path에 위도 경도 리스트를 동기화
                        path.setCoords(locationList);
                        // getMapAsync를 호출하여 비동기로 onMapReady 콜백 메서드 호출
                        // onMapReady에서 NaverMap 객체를 받음
                        finalMapFragment.getMapAsync(GamingActivity.this);
                    }
                }
            }
        });
        // TimerTask로 일정 시간마다 스케줄링할 작업 정의
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // map UI를 변경해야 하므로 Main Thread 호출
                GamingActivity.this.runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        // 동기화 버튼 자동클릭
                        btnstart.performClick();
                    }
                });
            }
        };
        // 5초 뒤부터 2초마다 실행
        Timer timer = new Timer();
        timer.schedule(timerTask, 5000, 2000);

        // Stop 버튼
        Button btnstop = findViewById(R.id.btnNormalStop);
        btnstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Timer kill
                timer.cancel();
                // Intert 생성 후 데이터 insert
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
        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        mNaverMap = naverMap;
        mNaverMap.setMapType(NaverMap.MapType.Basic);
        // 위도 경로 리스트의 크기가 3이상 일때, 마커와 경로 정의
        if (locationList.size() > 2) {
            try {
                // 이전의 마커가 있다면 삭제
                markers.get(0).setMap(null);
                markers.clear();
            } catch (Exception e) {
                // 이전의 마커가 없다면 pass
            }
            // 경로 정의
            path.setColor(Color.GREEN);
            path.setMap(mNaverMap);
            // 새 마커 정의
            Marker marker = new Marker();
            markers.add(marker);
            marker.setIcon(OverlayImage.fromResource(R.drawable.taco));
            marker.setWidth(150);
            marker.setHeight(120);
            marker.setPosition(locationList.get(locationList.size()-2));
            marker.setMap(mNaverMap);
        }
        // 지도 중심 현재 위치로 동기화
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
}
