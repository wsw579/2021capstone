package com.example.capstone.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.capstone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends BasicActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.sendButton).setOnClickListener(onClickListener);
        //영상에서는 loginButton인데 로그인 버튼 loginButton으로 했어요..
        // 대소문자 나중에 id.으로 찾을 때 참고 부탁드려요  - 승원
    }


    View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.sendButton:
                send();
                break;
        }
    };

    private void send() {
        String email = ((EditText) findViewById(R.id.emailEditText)).getText().toString();

        if (email.length() > 0) {
            final RelativeLayout loaderLayout = findViewById(R.id.loaderLayout);
            loaderLayout.setVisibility(View.VISIBLE);

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loaderLayout.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                startToast("이메일을 보냈습니다.");
                            }
                        }
                    });
        } else {
            startToast("이메일을 입력해주세요.");
        }
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}