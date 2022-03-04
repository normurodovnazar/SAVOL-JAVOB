package com.normurodov_nazar.adminformath.MyD;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.normurodov_nazar.savol_javob.Fragments.MyQuestions;
import com.normurodov_nazar.savol_javob.Fragments.NeedQuestions;
import com.normurodov_nazar.savol_javob.Fragments.PersonalChat;
import com.normurodov_nazar.savol_javob.Fragments.PublicQuestions;

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
