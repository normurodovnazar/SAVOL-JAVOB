package com.normurodov_nazar.savol_javob.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.normurodov_nazar.savol_javob.Activities.SearchUsers;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.MyD.UserListAdapter;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;
import java.util.List;

public class PersonalChat extends Fragment {
    RecyclerView recyclerView;
    ImageView addUsers;

    public PersonalChat() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void initVars(View v) {
        recyclerView = v.findViewById(R.id.recyclerViewHome);
        addUsers = v.findViewById(R.id.addUser);addUsers.setOnClickListener(x->startActivity(new Intent(getContext(), SearchUsers.class)));
        ArrayList<User> userList = new ArrayList<>();
        UserListAdapter adapter = new UserListAdapter(getContext(),userList,false);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_personal_chat, container, false);
        initVars(v);
        return v;
    }
}