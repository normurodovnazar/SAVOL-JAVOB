package com.normurodov_nazar.savol_javob.MyD;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;

public class DrawerItemsAdapter extends RecyclerView.Adapter<DrawerItemsAdapter.ItemViewHolder> {
    final Context context;
    final ArrayList<DrawerItem> drawerItems;
    final RecyclerViewItemClickListener listener;

    public DrawerItemsAdapter(Context context, ArrayList<DrawerItem> drawerItems, RecyclerViewItemClickListener listener) {
        this.context = context;
        this.drawerItems = drawerItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.drawer_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.setData(drawerItems.get(position),listener,position);
    }

    @Override
    public int getItemCount() {
        return drawerItems.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        ImageView image;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.drawerText);
            image = itemView.findViewById(R.id.drawerImage);
        }

        void setData(DrawerItem item,RecyclerViewItemClickListener listener,int i){
            title.setText(item.getTitleId());
            image.setImageResource(item.getImageId());
            itemView.setOnClickListener(v -> listener.onItemClick(null,itemView,i));
        }
    }

}
