package com.example.myguidefirebase;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.GuideViewHolder> {

    private Context context;
    private List<Guide> guideList;
    private OnGuideClickListener onGuideClickListener;

    public interface OnGuideClickListener {
        void onGuideClick(Guide guide);
    }

    public GuideAdapter(Context context, List<Guide> guideList, OnGuideClickListener onGuideClickListener) {
        this.context = context;
        this.guideList = guideList;
        this.onGuideClickListener = onGuideClickListener;
    }

    @NonNull
    @Override
    public GuideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_guide, parent, false);
        return new GuideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuideViewHolder holder, int position) {
        Guide guide = guideList.get(position);
        holder.textViewName.setText(guide.getName());
        holder.textViewPrice.setText(guide.getPricePerDay());
        holder.textViewServices.setText(TextUtils.join(", ", guide.getServices()));

        holder.itemView.setOnClickListener(v -> onGuideClickListener.onGuideClick(guide));
    }

    @Override
    public int getItemCount() {
        return guideList.size();
    }

    public static class GuideViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewPrice, textViewServices;

        public GuideViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewGuideName);
            textViewPrice = itemView.findViewById(R.id.textViewGuidePrice);
            textViewServices = itemView.findViewById(R.id.textViewGuideServices);
        }
    }
}
