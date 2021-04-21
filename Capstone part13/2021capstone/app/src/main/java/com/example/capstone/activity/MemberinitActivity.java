package com.example.capstone.activity;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.capstone.MemberInfo;
import com.example.capstone.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MemberinitActivity extends BasicActivity {

    private static final String TAG = "MemberinitActivity";
    private ImageView profileImageVIew;
    private String profilePath;
    private FirebaseUser user;
    private RelativeLayout loaderLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_init);

        loaderLayout = findViewById(R.id.loaderLayout);
        findViewById(R.id.checkButton).setOnClickListener(onClickListener);
        // activity_login.xml 보면 로그인 버튼 id를 checkButton이라고 했더라고요. 그래서 회원정보 확인 버튼은
        // checkButton_meminfo로 하려했는데 두 개가 연결되어있는지 하나 바꾸면 같이 바뀌네요.
        // 이유를 모르겠습니다만 일단 영상 그대로 login버튼이랑 회원정보에 확인 버튼이랑 id가 checkButton으로 똑같습니다.
        // 수정완료 - 수진
        profileImageVIew = findViewById(R.id.profileImageView);
        profileImageVIew.setOnClickListener(onClickListener);
        findViewById(R.id.imageModify).setOnClickListener(onClickListener);
        findViewById(R.id.vidioModify).setOnClickListener(onClickListener);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0: {
                if (resultCode == Activity.RESULT_OK) {
                    profilePath = data.getStringExtra("profilePath");
                    Log.e("로그", "profilePath: " + profilePath);

                    Glide.with(this).load(profilePath).centerCrop().override(500).into(profileImageVIew);
                }
                break;
            }
        }
    }

    View.OnClickListener onClickListener = (v) -> {

        switch (v.getId()) {
            case R.id.checkButton:
                storageUploader();
                break;
            case R.id.profileImageView:

                CardView cardView = findViewById(R.id.buttonsCardView);
                Log.e("로그", "로그" + cardView.getVisibility());
                if (cardView.getVisibility() == View.VISIBLE) {
                    cardView.setVisibility(View.GONE);
                } else {
                    cardView.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.imageModify:
                myStartActivity(CameraActivity.class);
                break;
            case R.id.vidioModify:

                if (ContextCompat.checkSelfPermission(MemberinitActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MemberinitActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            1);

                    if (ActivityCompat.shouldShowRequestPermissionRationale(MemberinitActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    } else {
                        startToast("권한을 허용해주세요");
                    }
                } else {
                    myStartActivity(GalleryActivity.class);
                }
                break;

        }
    };

    //part8 33:32 아니 이거 아무대도 안쓸꺼면 왜만든걸까요,,,, - 수진
    // 그런거 다 뒤에서 쓰더라구요... 아마 이것도 그러지않을까요  - 승원
    @Override
    public void onRequestPermissionsResult(int requestCode, String permission[], int[] grantResults) {

        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    myStartActivity(GalleryActivity.class);
                } else {
                    startToast("권한을 허용해주세요");
                }
            }
        }
    }

    private void storageUploader() {
        final String name = ((EditText) findViewById(R.id.nameEditText)).getText().toString();
        final String phoneNumber = ((EditText) findViewById(R.id.phoneNumberEditText)).getText().toString();
        // 원래 이 부분만 제일 앞에 알파벳이 대문자(=P)였는데
        // part5에서 PhoneNumber쓸일이있어서 그냥 다른거랑 똑같이 소문자로 통일해줬어요 -승원
        final String birthDay = ((EditText) findViewById(R.id.birthDayEditText)).getText().toString();
        // 영상은 birthDayEditText로 되어있는데 birthDayEditText3 오타인가요? 혹시 몰라서 수정안했어요. -승원
        // 오타 수정 완료 - 수진
        final String address = ((EditText) findViewById(R.id.addressEditText)).getText().toString();

        if (name.length() > 0 && phoneNumber.length() > 9 && birthDay.length() > 5 && address.length() > 0) {
            loaderLayout.setVisibility(View.VISIBLE);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            user = FirebaseAuth.getInstance().getCurrentUser();
            final StorageReference mountainImagesRef = storageRef.child("users/" + user.getUid() + "/profileImage.jpg");

            if (profilePath == null) {

                MemberInfo memberinfo = new MemberInfo(name, phoneNumber, birthDay, address);
                storeUploader(memberinfo);

            } else {
                try {
                    InputStream stream = new FileInputStream(new File(profilePath));
                    UploadTask uploadTask = mountainImagesRef.putStream(stream);
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@com.example.capstone.NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return mountainImagesRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@com.example.capstone.NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();

                                MemberInfo memberinfo = new MemberInfo(name, phoneNumber, birthDay, address, downloadUri.toString());
                                storeUploader(memberinfo);

                            } else {
                                // Handle failures
                                // ...
                                startToast("회원정보를 보내는데 실패하였습니다.");
                            }
                        }
                    });
                } catch (FileNotFoundException e) {
                    Log.e("로그", "에러" + e.toString());
                }
            }

        } else {
            startToast("회원정보를 입력해주세요.");
        }
    }

    private void storeUploader(MemberInfo memberinfo) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).set(memberinfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startToast("회원정보 등록을 성공하였습니다.");
                        loaderLayout.setVisibility(View.GONE);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        startToast("회원정보 등록에 실패하였습니다.");
                        loaderLayout.setVisibility(View.GONE);
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivityForResult(intent, 0);
    }
}
