package com.example.myrun;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.myrun.databinding.ActivityMainBinding;
import com.example.myrun.model.Auth;
import com.example.myrun.model.Firestore;
import com.example.myrun.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.FileInputStream;


public class MainActivity extends BaseActivity<ActivityMainBinding> {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    TextView tv_name;
    private static final String TAG = "MainActivity";
    public static Bitmap image_btn_click_tmp;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: {
                drawerLayout = findViewById(R.id.drawerLayout);
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected ActivityMainBinding getBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        View header = navigationView.getHeaderView(0);
        tv_name = (TextView) header.findViewById(R.id.tv_name);
        toolbar = findViewById(R.id.toolbar);

        //상단 툴바 설정
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true); // 커스터마이징 하기 위해 필요
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // 툴바 메뉴버튼 생성
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu); // 메뉴 버튼 모양 설정
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#55e6c3"))); // 툴바 배경색

        // 로그인 후 프로필 표시
        setProfile();


        // 네비게이션 뷰 아이템 클릭시 이뤄지는 이벤트
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                item.setChecked(true);
                drawerLayout.closeDrawers();

                int id = item.getItemId();
                // 각 메뉴 클릭시 이뤄지는 이벤트
                switch (id){
                    case R.id.item_main:
                        Intent intent_main = new Intent (MainActivity.this, MainActivity.class);
                        startActivity(intent_main);
                        finish();
                        break;

                    case R.id.item_profile:
                        Intent intent_calendar = new Intent (MainActivity.this, ProfileMainActivity.class);
                        startActivity(intent_calendar);
                        break;

                    case R.id.item_normal:
                        Intent intent_map = new Intent (MainActivity.this, MapActivity.class);
                        startActivity(intent_map);
                        break;

                    case R.id.item_game:
                        Intent intent_friend = new Intent (MainActivity.this, GamingActivity.class);
                        startActivity(intent_friend);
                        break;


                    case R.id.item_logout:
                        FirebaseAuth.getInstance().signOut();
                        Log.d(TAG,"sign-out success");
                        Intent intent_signOut = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent_signOut);
                        Toast.makeText(getApplicationContext(),"Logged out.",Toast.LENGTH_LONG).show();
                        break;
                }
                return true;
            }
        });
        // 메인의 공란 이미지 버튼들에, 일반 런닝 혹은 게임 모드에서 찍은 사진들을 stack
        ImageButton image_btn1_click = findViewById(R.id.image_btn1_click);
        ImageButton image_btn2_click = findViewById(R.id.image_btn2_click);

        image_btn1_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryRunning1__.class);
                startActivity(intent);
            }
        });

        image_btn1_click = findViewById(R.id.image_btn1_click);
        image_btn2_click = findViewById(R.id.image_btn2_click);

        // 일반 런닝모드와 게임 모드에서 찍은 사진을 비트맵화
        Bitmap bmp = null;
        String filename = getIntent().getStringExtra("image");
        try{
            FileInputStream is = this.openFileInput(filename);
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        // 일반 런닝모드와 게임 모드에서 찍은 사진을 bmp로 저장 후 덮어쓰기
        Intent receiveIntent = getIntent();
        int i = receiveIntent.getIntExtra("key",0);


        if (i == 0){
            image_btn1_click.setImageBitmap(bmp);
            image_btn_click_tmp = bmp;
        }
        else if(i == 1){
            image_btn1_click.setImageBitmap(image_btn_click_tmp);
            image_btn2_click.setImageBitmap(bmp);
        }

        // 일반 런닝모드와 게임 모드에서 달린 거리를 intent로 받아, 기록란에 나타냄
        TextView ttime = findViewById(R.id.ttime);
        TextView tkm = findViewById(R.id.tkm);
        TextView tkc = findViewById(R.id.tkc);
        if (receiveIntent.getStringExtra("time") != null) {
            ttime.setText(receiveIntent.getStringExtra("time"));
            tkm.setText(receiveIntent.getStringExtra("km"));
            tkc.setText(receiveIntent.getStringExtra("kc"));
        }
    }

    //툴바의 환영문구 세팅. 파이어베이스의 로그인한 이메일에서 split해 나타냄
    private void setProfile(){
        Firestore.getUserData(Auth.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);
                if(user != null){
                    tv_name.setText(user.getUserNickName() + "님");
                }else {
                    // failed.
                    Log.d("MainActivity.this", "user object is NULL.");
                }
            }
        });
    }
}