package com.example.finaltalk.fragment;

import android.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;


public class LogoutFragment extends Fragment {
    private FirebaseAuth firebaseAuth;

    public LogoutFragment() {
        firebaseAuth.signOut();
    }
}
