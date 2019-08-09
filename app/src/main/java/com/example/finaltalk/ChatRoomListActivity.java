package com.example.finaltalk;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class ChatRoomListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("ChatRoomListActivity", "ChatRoomListActivity 실행");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_list);
    }
}
