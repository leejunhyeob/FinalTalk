package com.example.finaltalk.fragment;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

//import com.example.finaltalk.chat.GroupMessageActivity;

public class ChatFragment extends Fragment {


    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm");

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        Log.d("chatfragment", "chatfragment 실행");
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.chatfragment_recyclerview);
        recyclerView.setAdapter(new ChatRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        return view;
    }


    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<ChatModel> chatModels = new ArrayList<>();
        private String uid;
        private List<String> keys = new ArrayList<>();
        private ArrayList<String> destinationUsers = new ArrayList<>();

        public ChatRecyclerViewAdapter() {

            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/" + uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    chatModels.clear();
                    //keys.clear();
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        chatModels.add(item.getValue(ChatModel.class));
                        keys.add(item.getKey());  //위 query 문에 해당하는걸 불러옴 -> firebase안에 챗룸에 자신의 uid가 해당된 챗룸을 가져옴
                        Log.d("chatfragment", "item.getKey()); " + item.getKey());
                    }
                    notifyDataSetChanged(); //recyclerview 업데이트
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
            // XML에 정의된 것들을 view의 형태로 변환
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

            final CustomViewHolder customViewHolder = (CustomViewHolder) holder;
            String destinationUid = null;

            // 일일 챗방에 있는 유저를 체크
            for (String user : chatModels.get(position).users.keySet()) {
                if (!user.equals(uid)) {
                    destinationUid = user;
                    destinationUsers.add(destinationUid);
                }
            }
            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);

                    customViewHolder.textView_title.setText(userModel.userName);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            //메시지를 내림 차순으로 정렬 후 마지막 메세지의 키값을 가져옴
            Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.reverseOrder());
            commentMap.putAll(chatModels.get(position).comments);

            if (commentMap.keySet().toArray().length > 0) {
                String lastMessageKey = (String) commentMap.keySet().toArray()[0];  //ArrayIndexOutOfBoundsException:
                customViewHolder.textView_last_message.setText(chatModels.get(position).comments.get(lastMessageKey).message);

                if (chatModels.get(position).users.size() > 2) {
                    customViewHolder.textView_people.setText("단체방");
                } else {
                    customViewHolder.textView_people.setText("개인방");
                }
                //TimeStamp
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                long unixTime = (long) chatModels.get(position).comments.get(lastMessageKey).timestamp;
                Date date = new Date(unixTime);
                customViewHolder.textView_timestamp.setText(simpleDateFormat.format(date));

            }
            customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.d("chatfragment", "N번째 방 클릭  -> N: " + position);
                    Log.d("chatfragment", "해당 방 인원수 : " + chatModels.get(position).users.size());

                    Log.d("chatfragment", "인원 Uid값 "+chatModels.get(position).users.toString());
                    Log.d("chatfragment", "채팅 내역 "+chatModels.get(position).comments.toString());

                    Intent intent = null;
                    if (chatModels.get(position).users.size() > 2) { //단체방
                        intent = new Intent(view.getContext(), GroupMessageActivity.class);
                        intent.putExtra("destinationRoom", keys.get(position)); //intent로 값 전달


                    } else {
                        intent = new Intent(view.getContext(), MessageActivity.class);
                        intent.putExtra("destinationUid", destinationUsers.get(position));

                    }

                    ActivityOptions activityOptions = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                        startActivity(intent, activityOptions.toBundle());
                    }
                }
            });
        }


        @Override
        public int getItemCount() {
            return chatModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {


            public TextView textView_title;
            public TextView textView_last_message;
            public TextView textView_timestamp;
            public TextView textView_people;

            public CustomViewHolder(View view) {
                super(view);

                textView_title = (TextView) view.findViewById(R.id.chatitem_textview_title);
                textView_last_message = (TextView) view.findViewById(R.id.chatitem_textview_lastMessage);
                textView_timestamp = (TextView) view.findViewById(R.id.chatitem_textview_timestamp);
                textView_people = (TextView) view.findViewById(R.id.chatitem_textview_people);
            }
        }
    }


}