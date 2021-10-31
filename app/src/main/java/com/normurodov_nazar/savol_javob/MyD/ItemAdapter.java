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

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemHolder> {
    final Context context;
    final ArrayList<Item> items;
    ArrayList<String> checked;
    final ItemClickListener listener;

    public void setChecked(ArrayList<String> checked) {
        this.checked = checked;
    }

    public ItemAdapter(Context context, ArrayList<Item> items, ArrayList<String> checked, ItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.checked = checked;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item,parent,false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.setItem(position,items.get(position),checked,listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemHolder extends RecyclerView.ViewHolder{
        TextView itemText;
        ImageView ch;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            itemText = itemView.findViewById(R.id.itemName);
            ch = itemView.findViewById(R.id.check);
        }

        void setItem(int pos,Item item,ArrayList<String> checked,ItemClickListener listener){
            itemText.setText(item.getName());
            ch.setBackgroundResource(checked.contains(item.getName()) ? R.drawable.floating_button_unpressed_bg : R.drawable.floating_button_pressed_bg);
            itemView.setOnClickListener(v -> listener.onItemClick(pos,item.getName()));
        }
    }
}
