package com.example.finaltalk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.finaltalk.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class SignupActivity extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM = 10;
    private EditText email;
    private EditText name;
    private EditText password;
    private Button signup;
    private String splash_background;
    private ImageView profile;
    private Uri imageUri;
    private Handler mHandler;
    private ProgressDialog mProgressDialog;
    private StorageReference mStorageRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) { //회원가입
        Log.d("SignupActivity","SignupActivity 실행");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mHandler = new Handler();

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        splash_background = mFirebaseRemoteConfig.getString(getString(R.string.rc_color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(splash_background));
        }


        profile = (ImageView)findViewById(R.id.signupActivity_imageview_profile);
        profile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });
        email = (EditText) findViewById(R.id.signupActivity_edittext_email);
        name = (EditText) findViewById(R.id.signupActivity_edittext_name);
        password = (EditText) findViewById(R.id.signupActivity_edittext_password);
        signup = (Button) findViewById(R.id.signupActivity_button_signup);
        signup.setBackgroundColor(Color.parseColor(splash_background));

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(TextUtils.isEmpty(name.getText().toString())){
                    Toast.makeText(SignupActivity.this,"plz enter your name",Toast.LENGTH_SHORT).show();
                    return;
                }else if(TextUtils.isEmpty(email.getText().toString())){
                    Toast.makeText(SignupActivity.this,"plz enter your email",Toast.LENGTH_SHORT).show();
                    return;
                }else if(TextUtils.isEmpty(password.getText().toString())){
                    Toast.makeText(SignupActivity.this,"plz enter your password",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(imageUri == null){
                    imageUri = Uri.parse("android.resource://"+getPackageName()+"/"+ R.drawable.image);
                }

                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){

                        mProgressDialog = ProgressDialog.show(SignupActivity.this,"","잠시만 기다려 주세요.",true);
                        mHandler.postDelayed( new Runnable() {  //handelr -> message나 runnable이라는 오브젝트를 전달/처리 해주는 역할
                            @Override
                            public void run() {
                                try {
                                    if (mProgressDialog!=null&&mProgressDialog.isShowing()){// mProgressDialog가 널이 아니고 보여지고있으면??
                                        mProgressDialog.dismiss();
                                    }
                                } catch ( Exception e ) {
                                    e.printStackTrace();
                                }
                            }
                        }, 10000);
                    }
                });


                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                final String uid = task.getResult().getUser().getUid();
                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name.getText().toString()).build();
                                task.getResult().getUser().updateProfile(userProfileChangeRequest);


                                FirebaseStorage.getInstance().getReference().child("userImages").child(uid).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        mStorageRef = FirebaseStorage.getInstance().getReference();
                                        mStorageRef.child("userImages").child(uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                UserModel userModel = new UserModel();
                                                userModel.userName = name.getText().toString();
                                                userModel.profileImageUrl = uri.toString();
                                                Log.d("imageurl","imageurl: "+uri.toString());
                                                userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                userModel.email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                                                FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        SignupActivity.this.finish();
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                UserModel userModel = new UserModel();
                                                userModel.userName = name.getText().toString();
                                                userModel.profileImageUrl = null;
                                                userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                userModel.email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                                                FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        SignupActivity.this.finish();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK){
            profile.setImageURI(data.getData());
            imageUri = data.getData();
        }
    }
}
