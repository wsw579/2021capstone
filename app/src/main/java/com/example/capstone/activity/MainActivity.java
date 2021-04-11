package com.example.capstone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.capstone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends BasicActivity {
    private static final String TAG="MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            myStartActivity(SignUpActivity.class);
        }else{          //회원가입 or 로그인 을 해서 메인 화면으로 넘어 온 경우
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if(document!=null) {
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d(TAG, "No such document");
                                myStartActivity(MemberinitActivity.class);
                            }
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

        }

        findViewById(R.id.LogoutButton).setOnClickListener(onClickListener);
        //영상에서는 logoutButton인데 로그아웃 버튼 LogoutButton으로 했어요..
        // 대소문자 나중에 id.으로 찾을 때 참고 부탁드려요  - 승원
        findViewById(R.id.floatingActionButton).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.LogoutButton:
                    FirebaseAuth.getInstance().signOut();
                    myStartActivity(SignUpActivity.class);
                    break;
                case  R.id.floatingActionButton:
                    myStartActivity(WritePostActivity.class);
                    break;

            }
        }
    };


    private void myStartActivity(Class c){
        Intent intent = new Intent(this, c);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //part5 영상 보다 보니까 이거 필요없다고 지우길래 지웠습니다. - 수진
        startActivity(intent);
    }
    }
