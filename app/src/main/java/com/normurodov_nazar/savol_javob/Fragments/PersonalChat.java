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
import com.google.firebase.firestore.ListenerRegistration;
import com.normurodov_nazar.savol_javob.Activities.AccountInformation;
import com.normurodov_nazar.savol_javob.Activities.SearchUsers;
import com.normurodov_nazar.savol_javob.Activities.SingleChat;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.ItemClickListener;
import com.normurodov_nazar.savol_javob.MyD.MyDialog;
import com.normurodov_nazar.savol_javob.MyD.MyDialogWithTwoButtons;
import com.normurodov_nazar.savol_javob.MyD.SuccessListener;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.MyD.UserListAdapter;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;
import java.util.Arrays;

public class PersonalChat extends Fragment {
    RecyclerView recyclerView;
    ImageView addUsers;
    ArrayList<User> users = new ArrayList<>();
    UserListAdapter adapter;
    ListenerRegistration registration;

    public PersonalChat() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initVars(View v) {
        adapter = new UserListAdapter(getContext(),new ArrayList<>(),false,null,null);
        recyclerView = v.findViewById(R.id.recyclerViewHome);
        addUsers = v.findViewById(R.id.addUser);
        addUsers.setOnClickListener(x -> startActivity(new Intent(getContext(), SearchUsers.class)));
        registration = Hey.setCollectionListener(getContext(), FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).collection(Keys.chats), docs -> {
            ArrayList<String> ids = new ArrayList<>();
            users = new ArrayList<>();
            for (DocumentSnapshot doc : docs) {
                ids.add(doc.getId());
            }
            setUsers(ids);
        }, errorMessage -> {

        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registration.remove();
    }

    private void setUsers(ArrayList<String> ids) {
        for (String id : ids) {
            Hey.getDocument(getContext(), FirebaseFirestore.getInstance().collection(Keys.users).document(id), doc -> {
                Hey.print("doc:", doc.toString());
                users.add(User.fromDoc((DocumentSnapshot) doc));
                if (ids.size()-1 == ids.indexOf(id)) showData();
            }, errorMessage -> {

            });
        }
    }

    private void showData() {
        adapter = new UserListAdapter(getContext(), users, false, (message, itemView, position) -> {
            Intent i = new Intent(getContext(), SingleChat.class);
            i.putExtra(Keys.chatId,Hey.getChatIdFromIds(My.id,users.get(position).getId()));
            startActivity(i);
        }, (message, itemView, position) -> Hey.showPopupMenu(getContext(), itemView, new ArrayList<>(Arrays.asList(getString(R.string.delete), getString(R.string.profileInfo))), (position1, name) -> {
            if (position1==0){
                MyDialogWithTwoButtons t = Hey.showDeleteDialog(getContext(),getString(R.string.deleteChatRequest).replace("xxx",users.get(position).getFullName()),null,false);
                t.setOnDismissListener(dialog -> {
                    if (t.getResult()) Hey.removeFromChats(getContext(), users.get(position).getId(), doc ->{});
                });
            }
            else {
                Intent info = new Intent(getContext(), AccountInformation.class);
                info.putExtra(Keys.id, String.valueOf(users.get(position).getId()));
                info.putExtra(Keys.fromChat, false);
                startActivity(info);
            }
        },true));
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

    MyDialog showError() {
        return Hey.showAlertDialog(getContext(), getString(R.string.error_unknown));
    }
}