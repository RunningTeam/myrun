package com.example.myrun;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myrun.model.Auth;
import com.example.myrun.model.Firestore;
import com.example.myrun.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;


public class ProfileMainActivity extends AppCompatActivity {

    private static final int LOGIN_CODE = 111; //값이 어느 액티비티에서 오는지 구별한다.
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_main);

        toolbar = findViewById(R.id.toolbar_map);
        //상단 툴바 설정
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true); // 커스터마이징 하기 위해 필요
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#55e6c3"))); // 툴바 배경색

        Button profile_edit_btn = findViewById(R.id.profile_edit_btn);
        profile_edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileMainActivity.this, ProfileEditActivity.class);
                startActivityForResult(intent, LOGIN_CODE);
            }
        });

        Button button_friends = findViewById(R.id.friends_add_btn);
        button_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileFriendsAddActivity.class);
                startActivity(intent);
            }
        });

        setProfile_name(); // 프로필 기본 이름을, 파이어베이스에 입력된 유저닉네임에서 불러오는 함수

//        Intent intent = getIntent();
//
//        String profile_name = intent.getStringExtra("profile_name");
//        String profile_licence = intent.getStringExtra("profile_licence");
//        String profile_gender = intent.getStringExtra("profile_gender");
//        String profile_address = intent.getStringExtra("profile_address");

    }

    private void setProfile_name(){
        TextView profile_name = (TextView) findViewById(R.id.receiveText1);
        Firestore.getUserData(Auth.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);
                profile_name.setText(user.getUserNickName().split("@")[0]);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        TextView profile_name = (TextView) findViewById(R.id.receiveText1);
        TextView profile_licence = (TextView) findViewById(R.id.receiveText2);
        TextView profile_gender = (TextView) findViewById(R.id.receiveText3);
        TextView profile_address = (TextView) findViewById(R.id.receiveText4);

        switch (requestCode){
            case LOGIN_CODE:
                if(resultCode == RESULT_OK) {
                    profile_name.setText(intent.getStringExtra("profile_name"));
                    profile_licence.setText(intent.getStringExtra("profile_licence"));
                    profile_gender.setText(intent.getStringExtra("profile_gender"));
                    profile_address.setText(intent.getStringExtra("profile_address"));
                }
                else {
                    Toast.makeText(this, "fail", Toast.LENGTH_LONG).show();
                }
                break;

            default:
                break;
        }
    }
}