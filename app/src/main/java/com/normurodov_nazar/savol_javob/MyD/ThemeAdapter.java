package com.normurodov_nazar.savol_javob.MyD;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;
import java.util.Map;

public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ThemeHolder> {
    final Context context;
    final ArrayList<Map<String,String>> themes;
    final boolean forSelect;
    final RecyclerViewItemClickListener listener;

    public ThemeAdapter(Context context, ArrayList<Map<String,String>> themes, boolean forSelect, RecyclerViewItemClickListener listener) {
        this.context = context;
        this.themes = themes;
        this.forSelect = forSelect;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ThemeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.theme_view, parent, false);
        return new ThemeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeHolder holder, int position) {
        holder.setTheme(themes.get(position).get(Keys.theme), forSelect, listener, position);
    }

    @Override
    public int getItemCount() {
        return themes.size();
    }

    static class ThemeHolder extends RecyclerView.ViewHolder {
        final TextView themeView;
        final ImageView status;
        final Context c;

        public ThemeHolder(@NonNull View itemView) {
            super(itemView);
            c = itemView.getContext();
            themeView = itemView.findViewById(R.id.themeText);
            status = itemView.findViewById(R.id.status);
        }

        void setTheme(String theme, boolean forSelect, RecyclerViewItemClickListener listener, int p) {
            themeView.setText(theme);
            String topicTheme = Hey.getTopicFromTheme(theme);
            Hey.print("topic",topicTheme);
            if (forSelect) status.setVisibility(View.INVISIBLE);
            else {
                SharedPreferences preferences = Hey.getPreferences(c);
                switch (p){
                    case 0:
                        if (preferences.getBoolean(Keys.sound,true)) setStar(); else removeStar();
                        break;
                    case 1:
                        if (preferences.getBoolean(Keys.vibrate,true)) setStar(); else removeStar();
                        break;
                    case 2:
                        if (preferences.getBoolean(Keys.privateChat,true)) setStar(); else removeStar();
                        break;
                    default:
                        if (preferences.getBoolean(topicTheme,false)) setStar(); else removeStar();
                        break;
                }
                status.setOnClickListener(view -> {
                    switch (p) {
                        case 0:
                            if (preferences.getBoolean(Keys.sound,true)) {
                                preferences.edit().putBoolean(Keys.sound,false).apply();
                                Hey.showToast(c,R.string.soundsDisabled);
                                removeStar();
                            } else {
                                preferences.edit().putBoolean(Keys.sound,true).apply();
                                Hey.showToast(c,R.string.soundsEnabled);
                                setStar();
                            }
                            break;
                        case 1:
                            if (preferences.getBoolean(Keys.vibrate,true)){
                                preferences.edit().putBoolean(Keys.vibrate,false).apply();
                                Hey.showToast(c,R.string.vibrateDisabled);
                                removeStar();
                            }else {
                                preferences.edit().putBoolean(Keys.vibrate,true).apply();
                                Hey.showToast(c,R.string.vibrateEnabled);
                                setStar();
                            }
                            break;
                        case 2:
                            if (preferences.getBoolean(Keys.privateChat,true)){
                                preferences.edit().putBoolean(Keys.privateChat,false).apply();
                                Hey.showToast(c,R.string.privateDisabled);
                                removeStar();
                            }else {
                                preferences.edit().putBoolean(Keys.privateChat,true).apply();
                                Hey.showToast(c,R.string.privateEnabled);
                                setStar();
                            }
                            break;
                        default:
                            LoadingDialog dialog = Hey.showLoadingDialog(c);
                            Hey.amIOnline(new StatusListener() {
                                @Override
                                public void online() {
                                    if (preferences.getBoolean(topicTheme,false)){
                                        FirebaseMessaging.getInstance().unsubscribeFromTopic(Keys.topics + topicTheme).addOnSuccessListener(unused -> {
                                            Hey.showToast(c,c.getString(R.string.notificationsDisabled).replace("aaa",theme));
                                            dialog.closeDialog();
                                            preferences.edit().putBoolean(topicTheme,false).apply();
                                            removeStar();
                                        }).addOnFailureListener(e -> {
                                            dialog.closeDialog();
                                            Hey.showToast(c,c.getString(R.string.error)+":"+e.getLocalizedMessage());
                                        });
                                    }else {
                                        FirebaseMessaging.getInstance().subscribeToTopic(Keys.topics + topicTheme).addOnSuccessListener(unused -> {
                                            Hey.showToast(c,c.getString(R.string.notificationEnabled).replace("aaa",theme));
                                            dialog.closeDialog();
                                            preferences.edit().putBoolean(topicTheme,true).apply();
                                            setStar();
                                        }).addOnFailureListener(e -> {
                                            dialog.closeDialog();
                                            Hey.showToast(c,c.getString(R.string.error)+":"+e.getLocalizedMessage());
                                        });
                                    }
                                }

                                @Override
                                public void offline() {
                                    Hey.showToast(itemView.getContext(), R.string.error_connection);
                                    dialog.closeDialog();
                                }
                            }, errorMessage -> dialog.closeDialog(), itemView.getContext());
                            break;
                    }
                });
            }

            themeView.setOnClickListener(view -> listener.onItemClick(null, itemView, p));
        }

        private void setStar() {
            status.setImageResource(R.drawable.ic_starred);
        }
        private void removeStar(){
            status.setImageResource(R.drawable.ic_no_starred);
        }
    }
}
