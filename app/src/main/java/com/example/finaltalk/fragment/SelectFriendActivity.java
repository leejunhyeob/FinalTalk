package com.example.finaltalk.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.finaltalk.R;
import com.example.finaltalk.chat.GroupMessageActivity;
import com.example.finaltalk.chat.MessageActivity;
import com.example.finaltalk.model.ChatModel;
import com.example.finaltalk.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectFriendActivity extends AppCompatActivity {
    ChatModel chatModel = new ChatModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("selectFriendactivity","selectFriendactivity 실행");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);

        RecyclerView recyclerView = findViewById(R.id.selectFriendActivity_recyclerview);
        recyclerView.setAdapter(new SelectFriendRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Button button = (Button) findViewById(R.id.selectFriendActivity_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                chatModel.users.put(myUid, true);
                Log.d("selectfriendactivity","myUid 추가 완료");
                FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel);
            }
        });
    }
    class SelectFriendRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        List<UserModel> userModels;

        public SelectFriendRecyclerViewAdapter() {
            userModels = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userModels.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        UserModel userModel = snapshot.getValue(UserModel.class);
                        userModels.add(userModel);
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }


        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_select, parent, false);
            //              안드로이드에서 view를 만드는 방법
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            String nUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if(nUid.equals(userModels.get(position).uid)){
                ((CustomViewHolder) holder).textView.setTextColor(Color.RED);
            }
            ((CustomViewHolder) holder).textView.setText(userModels.get(position).userName+"("+userModels.get(position).email+")");
            try{
                Uri imageurl =  Uri.parse(userModels.get(position).profileImageUrl);
                Glide.with(SelectFriendActivity.this).load(imageurl).into(((SelectFriendActivity.SelectFriendRecyclerViewAdapter.CustomViewHolder) holder).imageView);

            }catch (Exception e){
                return;
            }


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), GroupMessageActivity.class);
                    intent.putExtra("destinationUid", userModels.get(position).uid);
                    ActivityOptions activityOptions = null;

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                        startActivity(intent, activityOptions.toBundle());
                    }
                }
            });

                Log.d("selectfriendactivity","if문 추가 완료");
                ((CustomViewHolder) holder).checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        Log.d("selectfriendactivity","onCheckedChanged 추가 완료");
                        if(b){//true
                            chatModel.users.put(userModels.get(position).uid,true);
                            Log.d("selectfriendactivity","다른사람 Uid 추가 완료");
                        }else{//false
                            chatModel.users.remove(userModels.get(position));
                        }
                    }
                });


        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;
            public CheckBox checkBox;

            public CustomViewHolder(View view) {
                super(view);
                textView = (TextView) view.findViewById(R.id.frienditem_textview);
                checkBox = (CheckBox) view.findViewById(R.id.frienditem_checkbox);
                imageView = (ImageView) view.findViewById(R.id.frienditem_imageview);
            }
        }
    }
}
