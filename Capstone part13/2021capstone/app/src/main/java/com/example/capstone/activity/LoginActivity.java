package com.example.capstone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.capstone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends BasicActivity{
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.LoginButton).setOnClickListener(onClickListener);
        //영상에서는 loginButton인데 로그인 버튼 loginButton으로 했어요..
        // 대소문자 나중에 id.으로 찾을 때 참고 부탁드려요  - 승원

        // ? 이건 checkButton인데 loginButton은 왜,, 근데 checkButton 어디에 또 있었죠..? - 수진
        // 수정완료 - 수진

        findViewById(R.id.gotoPasswordResetButton).setOnClickListener(onClickListener);
    }


    View.OnClickListener onClickListener = (v)->{
            switch(v.getId()){
                case R.id.LoginButton:
                    login();
                    break;
                case R.id.gotoPasswordResetButton:
                    myStartActivity(PasswordResetActivity.class);
                    break;
            }
    };

    private void login() {
        String email = ((EditText) findViewById(R.id.emailEditText)).getText().toString();
        String password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString();

        if (email.length() > 0 && password.length() > 0) {
            final RelativeLayout loaderLayout = findViewById(R.id.loaderLayout);
            loaderLayout.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task){
                loaderLayout.setVisibility(View.GONE);
                if(task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    startToast("로그인에 성공하였습니다.");
                    myStartActivity(MainActivity.class);
                     //제 생각엔 이거 여기를 finish()하면 안될 것 같은데 어떻게 생각하시나요,,
                    // 왜 메인으로 안넘어가고 끝낸다는건지 이해 못하겠어요ㅠ
                    // part4 12:35 에서 이렇게 finish()로 바꿈 - 수진
                    // part5에서 수정완료 -승원
                } else {
                    if (task.getException() != null) {
                        startToast(task.getException().toString());
                    }
                }
            }
        });
        }else{
            startToast("이메일 또는 비밀번호를 입력해주세요.");
        }
    }

    private void startToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
    private void myStartActivity(Class c){
        Intent intent = new Intent(this, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}