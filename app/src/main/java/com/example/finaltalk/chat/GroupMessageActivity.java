package com.example.finaltalk.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finaltalk.R;
import com.example.finaltalk.model.ChatModel;
import com.example.finaltalk.model.NotificationModel;
import com.example.finaltalk.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupMessageActivity extends AppCompatActivity {
    Map<String, UserModel> users = new HashMap<>();
    String destinationRoom;
    String uid;
    EditText editText;


    private ValueEventListener valueEventListener;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    private UserModel destinationUserModel;
    private UserModel myUserModel;
    int peopleCount = 0;

    List<ChatModel.Comment> comments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("GroupMessageActivity", "GroupMessageActivity실행");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);
        destinationRoom = getIntent().getStringExtra("destinationRoom");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        editText = (EditText) findViewById(R.id.groupMessageActivity_editText);
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    users.put(item.getKey(), item.getValue(UserModel.class));
                }
                init();
                recyclerView = findViewById(R.id.groupMessageActivity_recyclerview);
                recyclerView.setAdapter(new GroupMessageRecyclerViewAdapter());
                recyclerView.setLayoutManager(new LinearLayoutManager(GroupMessageActivity.this));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Button outbutton = findViewById(R.id.groupMessageActivity_goout);
        outbutton.setOnClickListener(new View.OnClickListener() {
            ChatModel chatModel = new ChatModel();
            @Override
            public void onClick(View view) {
               //나가기 버튼 눌렸을때
            }
        });

    }

    void init() {
        Button button = (Button) findViewById(R.id.groupMessageActivity_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatModel.Comment comment = new ChatModel.Comment();
                comment.uid = uid;
                comment.message = editText.getText().toString();
                comment.timestamp = ServerValue.TIMESTAMP;
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("users").addListenerForSingleValueEvent(new ValueEventListener() { //users로 접근
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Map<String, Boolean> map = (Map<String, Boolean>) dataSnapshot.getValue(); //users 안에있는 아이디에 접근

                                for (String item : map.keySet()) {
                                    if (item.equals(uid)) { //자신 uid일 경우 알림 안보냄
                                        continue;
                                    }
                                    sendFcm(users.get(item).pushToken);
                                }
                                editText.setText(null);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

            }
        });
    }

    void sendFcm(String pushToken) {
        Log.d("messageactivity", "sendfcm 실행");
        Gson gson = new Gson();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();//보내는사람 이름 ->자기이름


        NotificationModel notificationModel = new NotificationModel();
        //notificationModel.to =  FirebaseDatabase.getInstance().getReference().child("users").child(destinatonUid).

        notificationModel.to = pushToken; //받는사람 토큰 id -> firebase database에서 가져와야함함
        notificationModel.notification.title = userName;
        notificationModel.notification.text = editText.getText().toString();
        notificationModel.data.title = userName;
        notificationModel.data.text = editText.getText().toString();


        RequestBody requestBody;
        requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel));

        Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .addHeader("Authorization", "key=AAAAoA9ZGIw:APA91bGiCXwLXctrY1eMUlQm_xh1zeRvSkH1J2FBdL-auP436KqFSYKcpzxpyMr1eAm0WfBkMqOLuvin_sMzSQZ8osD6udzXiGR9AhHRYtaR0iJE-yxvzq6eEGcOxIdzO9hAPqVbSHWA")
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) { //실패

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException { //성공

            }
        });
    }

    class GroupMessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public GroupMessageRecyclerViewAdapter() {
            getMessageList();
        }

        void getMessageList() {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("comments");
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear();
                    Map<String, Object> readUsersMap = new HashMap<>();

                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        ChatModel.Comment comment_motify = item.getValue(ChatModel.Comment.class);
                        comment_motify.readUsers.put(uid, true);

                        readUsersMap.put(key, comment_motify);
                        comments.add(comment_origin);

                    }
                    Log.d("값 확인", "comments size : " + comments.size());
                    if (comments.size() - 1 > 0) {
                        Log.d("messageactivity", "comments.size 가 0이 아님");
                        //-1
                        if (!comments.get(comments.size() - 1).readUsers.containsKey(uid)) {//여기
                            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("comments").updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    notifyDataSetChanged();//메세지 갱신
                                    recyclerView.scrollToPosition(comments.size() - 1);

                                }
                            });
                        } else {
                            Log.d("messageActivity", "아니면 여긴가");
                            notifyDataSetChanged();//메세지 갱신
                            recyclerView.scrollToPosition(comments.size() - 1);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);


            return new GroupMessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            GroupMessageViewHolder messageViewHolder = ((GroupMessageViewHolder) holder);

            //내가 보낸 메세지
            if (comments.get(position).uid.equals(uid)) {
                Glide.with(holder.itemView.getContext()).load(users.get(comments.get(position).uid).profileImageUrl).into(messageViewHolder.imageView_profile);
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.rightbubble);
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);
                messageViewHolder.textView_message.setTextSize(20);
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);
                setReadCounter(position, messageViewHolder.textView_readCounter_left);

                //상대방이 보낸 메세지
            } else {
                Glide.with(holder.itemView.getContext()).load(users.get(comments.get(position).uid).profileImageUrl).into(messageViewHolder.imageView_profile);
                messageViewHolder.textView_name.setText(users.get(comments.get(position).uid).userName);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble);
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setTextSize(20);
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);
                setReadCounter(position, messageViewHolder.textView_readCounter_right);

            }
            long unixTime = (long) comments.get(position).timestamp;  //1970년1월1일 부터 지난 밀리세컨드로 firebase에 저장되는데 이걸 계산해서 알아볼 수 있게 만들어야함
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);
            messageViewHolder.textView_timestamp.setText(time);

        }

        void setReadCounter(final int position, final TextView textView) {
            if (peopleCount == 0) {
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Boolean> users = (Map<String, Boolean>) dataSnapshot.getValue();
                        peopleCount = users.size();
                        int count = peopleCount - comments.get(position).readUsers.size();
                        if (count > 0) {
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(String.valueOf(count));
                        } else {
                            textView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else {
                int count = peopleCount - comments.get(position).readUsers.size();
                if (count > 0) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(String.valueOf(count));
                } else {
                    textView.setVisibility(View.INVISIBLE);
                }
            }
        }


        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class GroupMessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public TextView textView_timestamp;
            public TextView textView_readCounter_left;
            public TextView textView_readCounter_right;

            public GroupMessageViewHolder(View view) {
                super(view);

                textView_message = (TextView) view.findViewById(R.id.messageItem_textView_message);
                textView_name = (TextView) view.findViewById(R.id.messageitem_textview_name);
                linearLayout_destination = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_destination);
                linearLayout_main = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_main);
                textView_timestamp = (TextView) view.findViewById(R.id.messageItem_textView_timestamp);
                textView_readCounter_left = (TextView) view.findViewById(R.id.messageItem_textView_readCounter_left);
                textView_readCounter_right = (TextView) view.findViewById(R.id.messageItem_textView_readCounter_right);
                imageView_profile = (ImageView) view.findViewById(R.id.messageitem_imageview);

            }
        }
    }
}
