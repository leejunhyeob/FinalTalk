package com.example.finaltalk.fragment;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finaltalk.MainActitivy;
import com.example.finaltalk.R;
import com.example.finaltalk.SearchAdapter;
import com.example.finaltalk.chat.MessageActivity;
import com.example.finaltalk.model.UserModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class PeopleFragment extends Fragment {
    private StorageReference mStorageRef;
    private EditText search;
    private Button searchbtn;
    private DatabaseReference mDatabase;
    private Query productQuery;
    private List<String> list;          // 데이터를 넣은 리스트변수
    private ListView listView;          // 검색을 보여줄 리스트변수
    private EditText editSearch;        // 검색어를 입력할 Input 창
    private SearchAdapter adapter;      // 리스트뷰에 연결할 아답터
    private ArrayList<String> arraylist;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d("peoplefragment", "peoplefragment 실행");
        View view = inflater.inflate(R.layout.fragment_people, container, false);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.peoplefragment_recyclerview);
        search = (EditText) view.findViewById(R.id.peoplefragment_search);
        listView = (ListView) view.findViewById(R.id.listView);
        searchbtn = (Button) view.findViewById(R.id.peoplefragment_searchbtn);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter());


        FloatingActionButton floatingActionButton = (FloatingActionButton) view.findViewById(R.id.peoplefragment_floatingButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), SelectFriendActivity.class));
            }
        });

        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(search.getText().toString())) {
                    Log.d("peoplefragment", "검색칸 null");
                    return;
                } else {
                    //productQuery = mDatabase.orderByChild("userName").startAt(search.getText().toString()).endAt(search.getText().toString() + "\uf8ff");
                    //productQuery = mDatabase.child("users").orderByChild("name").equalTo(search.getText().toString());
                    //mDatabase.child("users").orderByChild("userName").equalTo(search.getText().toString());
                    //Log.d("peoplefragment", "productQuery:   " + productQuery);
                }
            }
        });

        return view;
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public TextView textView_comment;

        public CustomViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.frienditem_textview);
            imageView = view.findViewById(R.id.frienditem_imageview);
            textView_comment = view.findViewById(R.id.frienditem_textview_comment);
        }
    }

    class PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        List<UserModel> userModels;

        public PeopleFragmentRecyclerViewAdapter() {
            userModels = new ArrayList<>();
            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mStorageRef = FirebaseStorage.getInstance().getReference();

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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
            //              안드로이드에서 view를 만드는 방법
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

            String nUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Uri imageurl = Uri.parse(userModels.get(position).profileImageUrl);

            if (nUid.equals(userModels.get(position).uid)) {
                ((CustomViewHolder) holder).textView.setTextColor(Color.RED);
            }

            ((CustomViewHolder) holder).textView.setText(userModels.get(position).userName + "(" + userModels.get(position).email + ")");
            Glide.with(PeopleFragment.this).load(imageurl).into(((CustomViewHolder) holder).imageView);


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), MessageActivity.class);
                    intent.putExtra("destinationUid", userModels.get(position).uid);
                    ActivityOptions activityOptions = null;

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                        startActivity(intent, activityOptions.toBundle());
                    }
                }
            });
            if (userModels.get(position).comment != null) {
                ((CustomViewHolder) holder).textView_comment.setText(userModels.get(position).comment);
            }
        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }

    }
}
