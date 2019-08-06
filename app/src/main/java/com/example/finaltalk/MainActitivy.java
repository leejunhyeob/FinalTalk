package com.example.finaltalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.finaltalk.fragment.ChatFragment;
import com.example.finaltalk.fragment.PeopleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;



import java.util.HashMap;
import java.util.Map;

public class MainActitivy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {// 유저 리스트
        Log.d("MainActitivy","MainActitivy 실행");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_main);


        Log.d("MainActitivy","여기를 다시오는건가");

        passPushTokenToServer();
        getFragmentManager().beginTransaction().replace(R.id.submainactivity_framelayout,new PeopleFragment()).commit();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.submainactivity_bottomnavigetionview);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem Item) {
                Log.d("MainActitivy","네비게이션");
                switch (Item.getItemId()){
                    case R.id.action_people:
                        getFragmentManager().beginTransaction().replace(R.id.submainactivity_framelayout, new PeopleFragment()).commit();

                        Intent intent = new Intent(MainActitivy.this, MainActitivy.class);
                        startActivity(intent);
                        return true;

                    case R.id.action_logout:

                        Log.d("MainActitivy","로그아웃 버튼 눌림");

                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 로그인한 uid
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        Map<String, Object> map = new HashMap<>(); //firebase 토큰은 해쉬맵으로만 가져올수가 있다함
                        map.put("pushToken",null); // 기기 토큰값을 넣어줌
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map);
                        auth.signOut();

                        Log.d("MainActitivy","로그아웃 완료");

                        intent = new Intent(MainActitivy.this, LoginActivity.class);
                        startActivity(intent);

                        Log.d("MainActitivy","로그인창으로 이동");
                        //MainActitivy.this.finish();
                        finish();
                        return true;

                    case R.id.action_chat:


                        Log.d("MainActitivy","챗 버튼 눌림");

                        getFragmentManager().beginTransaction().replace(R.id.submainactivity_framelayout, new ChatFragment()).commit();
                        return true;
                }
                return false;
            }
        });


        //activity 내의 fragment관리, 프래그먼트 가져옴
    }

        void passPushTokenToServer(){
        Log.d("submainactivity","토큰값 넣기~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        @NonNull
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 로그인한 uid
        String token = FirebaseInstanceId.getInstance().getToken();//해당 기기 토큰
        Map<String, Object> map = new HashMap<>(); //firebase 토큰은 해쉬맵으로만 가져올수가 있다함
        map.put("pushToken",token); // 기기 토큰값을 넣어줌
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map); //여따가
    }
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }




}
