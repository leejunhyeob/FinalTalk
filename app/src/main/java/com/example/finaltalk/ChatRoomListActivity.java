package com.example.finaltalk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class ChatRoomListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("ChatRoomListActivity","ChatRoomListActivity 실행");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_list);
    }
}
