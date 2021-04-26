package com.example.capstone.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.capstone.R;
import com.example.capstone.PostInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;


public class WritePostActivity extends BasicActivity{

    private static final String TAG = "WritePostActivity";
    private FirebaseUser user;
    private ArrayList<String> pathList =new ArrayList<>();
    private LinearLayout parent;
    private RelativeLayout buttonBackgroundLayout;
    private RelativeLayout loaderLayout;
    private ImageView selectImageView;
    private EditText selectedEditText;
    private int pathCount, successCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wirte_post);

        parent = findViewById(R.id.contentsLayout);
        buttonBackgroundLayout = findViewById(R.id.buttonsBackgroundLayout);
        loaderLayout = findViewById(R.id.loaderLayout);

        buttonBackgroundLayout.setOnClickListener(onClickListener);
        findViewById(R.id.check).setOnClickListener(onClickListener);
        findViewById(R.id.image).setOnClickListener(onClickListener);
        findViewById(R.id.video).setOnClickListener(onClickListener);
        findViewById(R.id.imageModify).setOnClickListener(onClickListener);
        findViewById(R.id.vidioModify).setOnClickListener(onClickListener);
        findViewById(R.id.delete).setOnClickListener(onClickListener);
        findViewById(R.id.contentsEditText).setOnFocusChangeListener(onFocusChangeListener);
        findViewById(R.id.titleEditText).setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                if(hasFocus){
                    selectedEditText = null;
                }
            }
        });


    }

    public void onActivityResult(int requestCode,int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    String profilePath = data.getStringExtra("profilePath");
                    pathList.add(profilePath);

                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    LinearLayout linearLayout = new LinearLayout(WritePostActivity.this);
                    linearLayout.setLayoutParams(layoutParams);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);

                    if(selectedEditText == null){
                        parent.addView(linearLayout);
                    }else {
                        for(int i = 0; i<parent.getChildCount(); i++){
                            if(parent.getChildAt(i) == selectedEditText.getParent()){
                                parent.addView(linearLayout, i+1);
                                break;

                            }
                        }
                    }

                    parent.addView(linearLayout);

                    ImageView imageView = new ImageView(WritePostActivity.this);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buttonBackgroundLayout.setVisibility(View.VISIBLE);
                            selectImageView = (ImageView) v;
                        }
                    });
                    Glide.with(this).load(profilePath).override(1000).into(imageView);
                    linearLayout.addView(imageView);

                    EditText editText = new EditText(WritePostActivity.this);
                    editText.setLayoutParams(layoutParams);
                    editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
                    editText.setHint("내용");
                    editText.setOnFocusChangeListener(onFocusChangeListener);
                    linearLayout.addView(editText);
                }
                    break;

                case 1:

                    if (resultCode == Activity.RESULT_OK) {
                        String profilePath = data.getStringExtra("profilePath");

                        Glide.with(this).load(profilePath).override(1000).into(selectImageView);

                    }
                    break;
        }
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v){
            switch (v.getId()){
                case R.id.check:
                    storageUpload();
                    startToast("check");
                    break;
                case R.id.image:
                    myStartActivity(GalleryActivity.class,"image",  0);
                    break;
                case R.id.video:
                    myStartActivity(GalleryActivity.class,"video", 0);
                    break;
                case R.id.buttonsBackgroundLayout:
                    if(buttonBackgroundLayout.getVisibility() == View.VISIBLE){
                        buttonBackgroundLayout.setVisibility(View.GONE);
                    }
                case R.id.imageModify:
                    myStartActivity(GalleryActivity.class,"image", 1);
                    buttonBackgroundLayout.setVisibility(View.GONE);
                    break;
                case R.id.vidioModify:
                    myStartActivity(GalleryActivity.class,"vidio", 1);
                    buttonBackgroundLayout.setVisibility(View.GONE);
                    break;
                case R.id.delete:
                    parent.removeView((View)selectImageView.getParent());
                    buttonBackgroundLayout.setVisibility(View.GONE);
                    break;
            }

        }
    };


    private void storageUpload() {
        final String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();
        //final String contents = ((EditText) findViewById(R.id.contentsEditText)).getText().toString();

        if (title.length() > 0) {
            loaderLayout.setVisibility(View.VISIBLE);
            final ArrayList<String> contentsList= new ArrayList<>();
            user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            final DocumentReference documentReference = firebaseFirestore.collection("posts").document();

            for(int i=0; i<parent.getChildCount(); i++){
                LinearLayout linearLayout = (LinearLayout)parent.getChildAt(i);

                for(int ii = 0; ii<linearLayout.getChildCount(); ii++){

                    View view = linearLayout.getChildAt(ii);
                    if(view instanceof EditText){
                        //String text= contents;
                        String text = ((EditText)view).getText().toString();
                        if(text.length() > 0){
                            contentsList.add(text);
                            Log.e(TAG, "here111");
                        }
                        //WriteInfo writeinfo = new WriteInfo(title, contentsList, user.getUid(), new Date());
                        //storeUpload(writeinfo);
                        Log.e(TAG, "here222");

                        //else 복붙 해도 안올라가긴 함

                        //복붙끝

                    } else{
                        Log.e(TAG, "here333");
                        contentsList.add(pathList.get(pathCount));
                        final StorageReference mountainImagesRef = storageRef.child("posts/" + documentReference.getId() + "/" + pathCount +".jpg");
                        try{
                            InputStream stream = new FileInputStream(new File(pathList.get(pathCount)));
                            StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("index",""+(contentsList.size()-1)).build();
                            UploadTask uploadTask= mountainImagesRef.putStream(stream, metadata);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                    Log.e(TAG, "here444");
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    final int index = Integer.parseInt(taskSnapshot.getMetadata().getCustomMetadata("index"));
                                    mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Log.e("로그","uri :" +uri);
                                            contentsList.set(index, uri.toString());
                                            successCount++;
                                            if (pathList.size() == successCount) {
                                                //완료
                                                Log.e(TAG, "here222");
                                                PostInfo writeinfo = new PostInfo(title, contentsList, user.getUid(), new Date());
                                                storeUpload(documentReference, writeinfo);
                                                for (int a = 0; a < contentsList.size(); a++){
                                                    Log.e("로그", "콘텐츠: " + contentsList.get(a));
                                                }
                                            }
                                        }
                                    });
                                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                }
                            });
                        } catch (FileNotFoundException e){
                            Log.e("로그","에러"+e.toString());
                        }
                        pathCount++;
                    }
                }
            }
            if(pathList.size() == 0){
                PostInfo postInfo = new PostInfo(title,contentsList,user.getUid(), new Date());
                storeUpload(documentReference, postInfo);
            }
        }else{
            startToast("제목을 입력해주세요.");
        }
    }


    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener(){
        @Override
        public void onFocusChange(View v, boolean hasFocus){
            if(hasFocus){
                selectedEditText = (EditText)v;
            }
        }
    };
    private void storeUpload(DocumentReference documentReference, PostInfo postInfo){
        documentReference.set(postInfo)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Document writeen");
                loaderLayout.setVisibility(View.GONE);
                finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.d(TAG, "Error");
                    }

                });
        /* part 12에서 날림 아직 else 안고쳐놔서 일단 주석처리함
        FirebasFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").add(writeInfo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.e(TAG, "documentsnapshot written with id: "+ documentReference.getId());
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

         */
    }

    private void startToast(String msg){ Toast.makeText(this,msg,Toast.LENGTH_SHORT).show(); }

    private void  myStartActivity(Class c,String media, int requestCode){
        Intent intent = new Intent(this,c);
        intent.putExtra("media",media);
        startActivityForResult(intent,requestCode);
    }
}
