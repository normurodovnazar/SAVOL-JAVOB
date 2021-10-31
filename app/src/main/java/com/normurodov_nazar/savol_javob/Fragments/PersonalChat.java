package com.normurodov_nazar.savol_javob.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.Activities.SearchUsers;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.MyDialog;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.MyD.UserListAdapter;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;

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
        Hey.collectionListener(getContext(), FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).collection(Keys.chats), docs -> {
            ArrayList<String> ids = new ArrayList<>();
            for (DocumentSnapshot doc:docs){
                ids.add(doc.getId());
            }
            setUsers(ids);
        }, errorMessage -> {

        });
    }

    private void setUsers(ArrayList<String> ids) {
        ArrayList<User> users = new ArrayList<>();
        for(String id : ids){
            Hey.addDocumentListener(getContext(), FirebaseFirestore.getInstance().collection(Keys.users).document(id), doc -> {
                Hey.print("doc:",doc.toString());
                users.add(User.fromDoc(doc));
                UserListAdapter adapter = new UserListAdapter(getContext(),users,false);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }, errorMessage -> {

            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_personal_chat, container, false);
        initVars(v);
        return v;
    }

    MyDialog showError(){
        return Hey.showAlertDialog(getContext(),getString(R.string.error_unknown));
    }
}