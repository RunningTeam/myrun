package com.example.myrun;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myrun.model.Firestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {
    // 전역변수 선언
    // Firebase 유저 정보와 로그 Tag
    private static final String TAG = "SignUpActivity";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // 유저정보 초기화
        mAuth = FirebaseAuth.getInstance();

        // 회원가입 버튼
        Button signUpbtn = findViewById(R.id.signUpButton);
        signUpbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        // 로그인 버튼
        Button gotoLoginbtn = findViewById(R.id.gotoLoginButton);
        gotoLoginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoginActivity();
            }
        });
    }

    private void signUp(){
        // 이메일과 패스워드, 닉네임 받아오기
        EditText e_text = (EditText)findViewById(R.id.emailEditText);
        EditText p_text = (EditText)findViewById(R.id.passwordEditText);
        EditText p_ch_text = (EditText)findViewById(R.id.passwordCheckEditText);

        String email = e_text.getText().toString();
        String password = p_text.getText().toString();
        String passwordCheck = p_ch_text.getText().toString();

        if (email.length()>0 && password.length()>0 && passwordCheck.length()>0) {

            if (password.equals(passwordCheck)) {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // 유저정보 생성 성공
                                    // DB 생성 작업
                                    createNewUserDatabase(task.getResult().getUser());
                                } else {
                                    // 유저정보 생성 실패
                                    if (task.getException() != null) {
                                        startToast("파이어베이스 연동 실패");
                                    }
                                }
                            }
                        });
            } else {
                startToast("비밀번호가 일치하지 않습니다.");
            }
        }else {
            startToast("이메일 또는 비밀번호를 입력해 주세요.");
        }
    }

    // firebase DB에 새 유저정보 작성
    private void createNewUserDatabase(FirebaseUser user) {
        // 새 유저 정보 작성
        Firestore.writeNewUser(user.getUid(), user.getEmail(), user.getEmail())
                .addOnCompleteListener(documentTask -> {
                    // 성공했다면
                    if(documentTask.isSuccessful()) {
                        startToast("회원가입 성공");
                    }
                    // 실패했다면
                    else {
                        // 에러 메시지 띄우고 로그아웃
                        Toast.makeText(getApplicationContext(), "회원가입 실패", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                });
    }
    // 메세지 함수
    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    // 로그인 액티비티 실행
    private void startLoginActivity(){
        Intent intent = new Intent (this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

