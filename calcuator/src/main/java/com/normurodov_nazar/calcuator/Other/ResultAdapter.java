package com.normurodov_nazar.calcuator.Other;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.normurodov_nazar.calcuator.Hey;
import com.normurodov_nazar.calcuator.R;

import java.util.ArrayList;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.MHolder> {

    final Context context;
    final ArrayList<String> items;
    final RecyclerListener listener;
    final ArrayList<MHolder> holders = new ArrayList<>();

    public ResultAdapter(Context context, ArrayList<String> items, RecyclerListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.text, parent, false);
        return new MHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MHolder holder, int position) {
        holders.add(holder);
        holder.setData(position, items.get(position), listener);
    }

    public void setTextSize(float f) {
        for (MHolder holder : holders) holder.setTextSize(f);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MHolder extends RecyclerView.ViewHolder {
        final TextView tx;

        public MHolder(@NonNull View itemView) {
            super(itemView);
            tx = itemView.findViewById(R.id.tx);
            setTextSize(Hey.size);
        }

        void setData(int i, String s, RecyclerListener listener) {
            tx.setText(s);
            itemView.setOnClickListener(v -> listener.onClick(i, itemView, s));
        }

        void setTextSize(float f) {
            tx.setTextSize(TypedValue.COMPLEX_UNIT_PX, f * 0.5f);
        }
    }
}
