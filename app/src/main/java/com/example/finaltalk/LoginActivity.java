package com.example.finaltalk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class LoginActivity extends AppCompatActivity {


    private EditText id;
    private EditText password;

    private Button login;
    private Button signup;
    private FirebaseRemoteConfig FirebaseRemoteConfig;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {  //로그인
        Log.d("LoginActivity", "LoginActivity 실행");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();


        String splash_background = FirebaseRemoteConfig.getString(getString(R.string.rc_color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(splash_background));
        }

        id = (EditText) findViewById(R.id.loginActivity_edittext_id);
        password = (EditText) findViewById(R.id.loginActivity_edittext_password);


        login = (Button) findViewById(R.id.loginActivity_button_login);
        signup = (Button) findViewById(R.id.loginActivity_button_signup);
        login.setBackgroundColor(Color.parseColor(splash_background));
        signup.setBackgroundColor(Color.parseColor(splash_background));

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginEvent();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });


        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //로그인
                    Intent intetn = new Intent(LoginActivity.this, MainActitivy.class);
                    startActivity(intetn);
                    finish();
                } else {
                    //로그아웃
                }
            }
        };
    }

    void loginEvent() {
        //로그인할때 값이 null인지 확인 필요
        if (TextUtils.isEmpty(id.getText().toString())) {
            Toast.makeText(LoginActivity.this, "plz enter your EMAIL", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(password.getText().toString())) {
            Toast.makeText(LoginActivity.this, "plz enter your password", Toast.LENGTH_SHORT).show();
            return;
        }
        final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("계정 확인중");

        dialog.show();

        firebaseAuth.signInWithEmailAndPassword(id.getText().toString().trim(), password.getText().toString().trim())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                        //LoadingActivity.passPushTokenToServer();
                        dialog.dismiss();

                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

}
