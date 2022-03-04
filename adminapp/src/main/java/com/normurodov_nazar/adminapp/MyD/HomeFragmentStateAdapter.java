package com.normurodov_nazar.adminapp.MyD;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.normurodov_nazar.adminapp.Fragments.MyQuestions;
import com.normurodov_nazar.adminapp.Fragments.NeedQuestions;
import com.normurodov_nazar.adminapp.Fragments.PersonalChat;
import com.normurodov_nazar.adminapp.Fragments.PublicQuestions;

public class HomeFragmentStateAdapter extends FragmentStateAdapter {

    public HomeFragmentStateAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0: return new PersonalChat();
            case 1: return new PublicQuestions();
            case 2:return new MyQuestions();
            default:return new NeedQuestions();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
