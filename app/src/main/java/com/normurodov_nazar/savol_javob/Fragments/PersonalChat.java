package com.normurodov_nazar.savol_javob.Fragments;

import static com.normurodov_nazar.savol_javob.MFunctions.Hey.gotoPrivateChat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.normurodov_nazar.savol_javob.Activities.AccountInformation;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.MyDialogWithTwoButtons;
import com.normurodov_nazar.savol_javob.MyD.UserListAdapter;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;
import java.util.Arrays;

public class PersonalChat extends Fragment {
    RecyclerView recyclerView;
    ProgressBar bar;
    TextView text;
    ArrayList<Long> userIds = new ArrayList<>();
    UserListAdapter adapter;
    ListenerRegistration registration;


    public PersonalChat() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initVars(View v) {
        bar = v.findViewById(R.id.barPersonal);
        text = v.findViewById(R.id.noChats);
        adapter = new UserListAdapter(getContext(),new ArrayList<>(), null,null);
        recyclerView = v.findViewById(R.id.recyclerViewHome);
        registration = Hey.setCollectionListener(getContext(), FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).collection(Keys.chats), docs -> {
            userIds.clear();
            userIds = new ArrayList<>();
            for (DocumentSnapshot doc : docs) {
                userIds.add(Long.parseLong(Hey.getFriendsIdFromChatId(doc.getId())));
            }
            if (userIds.size()==0) showNoChats(); else showData();
        }, errorMessage -> {

        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registration.remove();
    }

    private void showData() {
        adapter = new UserListAdapter(getContext(), userIds, user -> gotoPrivateChat(getContext(),user.getId()), user -> Hey.showPopupMenu(getContext(), bar, new ArrayList<>(Arrays.asList(getString(R.string.delete), getString(R.string.profileInfo))), (position1, name) -> {
            if (position1==0){
                MyDialogWithTwoButtons t = Hey.showDeleteDialog(getContext(),getString(R.string.deleteChatRequest).replace("xxx",user.getName()),null,false);
                t.setOnDismissListener(dialog -> {
                    if (t.getResult()) Hey.removeFromChats(getContext(), user.getId(), doc ->{});
                });
            }
            else {
                Intent info = new Intent(getContext(), AccountInformation.class);
                info.putExtra(Keys.id, String.valueOf(user.getId()));
                //info.putExtra(Keys.fromChat, false);
                startActivity(info);
            }
        },true));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        showChats();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_personal_chat, container, false);
        initVars(v);
        showLoading();
        return v;
    }

    void showChats(){
        recyclerView.setVisibility(View.VISIBLE);
        bar.setVisibility(View.INVISIBLE);
        text.setVisibility(View.INVISIBLE);
    }
    void showNoChats(){
        recyclerView.setVisibility(View.INVISIBLE);
        bar.setVisibility(View.INVISIBLE);
        text.setVisibility(View.VISIBLE);
    }

    void showLoading(){
        recyclerView.setVisibility(View.INVISIBLE);
        bar.setVisibility(View.VISIBLE);
        text.setVisibility(View.INVISIBLE);
    }
}