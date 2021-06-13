package com.example.myrun;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import java.io.InputStream;


public class ProfileMainActivity extends AppCompatActivity {

    private static final int LOGIN_CODE = 111; //값이 어느 액티비티에서 오는지 구별한다.
    private static final int REQUEST_CODE = 0;
    Toolbar toolbar;
    ImageView profile_image;

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

        //이미지 뷰를 클릭시, 내부 저장소의 사진을 프로필로 저장 할 수있게끔 하는 메서드
        profile_image = findViewById(R.id.profile_image);
        profile_image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    private void setProfile_name(){
        TextView profile_name = (TextView) findViewById(R.id.receiveText1);
        Firestore.getUserData(Auth.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);
                profile_name.setText(user.getUserNickName().split("@")[0]); // 아이디 중, @을 기준, 왼쪽에 있는 아이디를 이름으로 임시 설정
            }
        });
    }

    // 편집 레이아웃에서, 입력된 값들을 프로필에 올려주는 메서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        TextView profile_name = (TextView) findViewById(R.id.receiveText1);
        TextView profile_licence = (TextView) findViewById(R.id.receiveText2);
        TextView profile_gender = (TextView) findViewById(R.id.receiveText3);
        TextView profile_address = (TextView) findViewById(R.id.receiveText4);

        switch (requestCode){
            case LOGIN_CODE: // case, 프로필 편집일 때
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
            case REQUEST_CODE: // case, 프로필 사진 변경일 때
                if(resultCode == REQUEST_CODE){
                        if(resultCode == RESULT_OK) {
                            try{
                                InputStream in = getContentResolver().openInputStream(intent.getData());

                                Bitmap img = BitmapFactory.decodeStream(in);
                                in.close();

                                profile_image.setImageBitmap(img);
                            }catch(Exception e)
                            { }
                        }
                        else if(resultCode == RESULT_CANCELED) {
                            Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        }
}