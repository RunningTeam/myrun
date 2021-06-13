package com.example.myrun;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProfileEditActivity extends AppCompatActivity {
    private static final int LOGIN_CODE = 111; //값이 어느 액티비티에서 오는지 구별한다.
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        final EditText profile_name = findViewById(R.id.profile_name);
        final EditText profile_licence = findViewById(R.id.profile_licence);
        final EditText profile_gender = findViewById(R.id.profile_gender);
        final EditText profile_address = findViewById(R.id.profile_address);

        toolbar = findViewById(R.id.toolbar_map);
        //상단 툴바 설정
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true); // 커스터마이징 하기 위해 필요
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#55e6c3"))); // 툴바 배경색

        Button save_profile = findViewById(R.id.save_profile); // 프로필 편집 후, 관련 정보를 넘기는 메소드
        save_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileMainActivity.class);

                intent.putExtra("profile_name", profile_name.getText().toString()); //id 전달
                intent.putExtra("profile_licence", profile_licence.getText().toString()); //회원번호 전달
                intent.putExtra("profile_gender", profile_gender.getText().toString()); // 성별 전달
                intent.putExtra("profile_address", profile_address.getText().toString()); // 주소 전달

                setResult(RESULT_OK, intent);

                finish();
            }
        });
    }
}