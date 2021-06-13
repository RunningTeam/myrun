package com.example.myrun;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    // 전역변수 선언
    // Firebase 유저 정보와 로그 Tag
    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //회원가입 버튼
        Button signUpBtn = findViewById(R.id.signUpButton);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 유저정보 초기화
        mAuth = FirebaseAuth.getInstance();

        //로그인 버튼
        Button loginbtn = findViewById(R.id.loginButton);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        // 이메일과 패스워드 받아오기
        EditText e_text = (EditText)findViewById(R.id.emailEditText);
        EditText p_text = (EditText)findViewById(R.id.passwordEditText);

        String email = e_text.getText().toString();
        String password = p_text.getText().toString();

        // 이메일과 패스워드가 빈칸이 아닐경우
        if (email.length()>0 && password.length()>0 ) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // 로그인 성공시
                                Log.d(TAG, "signInWithEmail:success");
                                startToast("로그인 성공");
                                startMainActivity();  // MainActivity로 이동
                                finish();
                            } else {
                                // 실패시
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                startToast("이메일과 패스워드를 확인해주세요.");
                                if(task.getException() != null){
                                    startToast("파이어베이스 연동 실패");
                                }
                            }
                        }
                    });
        } else {
            // 이메일과 패스워드가 빈칸일 경우
            startToast("이메일과 패스워드를 입력해주세요.");
        }
    }

    private void startToast(String msg) {
        // 메세지 함수
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void startMainActivity(){
        // 메인 액티비티 실행
        Intent intent = new Intent (this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}

