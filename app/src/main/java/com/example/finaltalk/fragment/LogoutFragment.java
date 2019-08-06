package com.example.finaltalk.fragment;

import android.app.Fragment;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


import java.util.HashMap;
import java.util.Map;


public class LogoutFragment extends Fragment {
    private FirebaseAuth firebaseAuth;

    public LogoutFragment() {
        firebaseAuth.signOut();
    }
}
