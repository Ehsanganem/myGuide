package com.example.myguidefirebase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PendingCertificationsAdapter extends RecyclerView.Adapter<PendingCertificationsAdapter.ViewHolder> {

    private final List<Certification> pendingCertifications;
    private final OnCertificationActionListener actionListener;

    public PendingCertificationsAdapter(List<Certification> pendingCertifications, OnCertificationActionListener actionListener) {
        this.pendingCertifications = pendingCertifications;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_certification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Certification certification = pendingCertifications.get(position);
        holder.textViewUserName.setText(certification.getUserName() != null ? certification.getUserName() : "No name provided");
        holder.textViewUserEmail.setText(certification.getUserEmail());
        holder.textViewStatus.setText(certification.getStatus());

        holder.itemView.setOnClickListener(v -> actionListener.onViewDetails(certification));
    }

    @Override
    public int getItemCount() {
        return pendingCertifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewUserName, textViewUserEmail, textViewStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            textViewUserEmail = itemView.findViewById(R.id.textViewUserEmail);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
        }
    }

    public interface OnCertificationActionListener {
        void onViewDetails(Certification certification);
    }
}
