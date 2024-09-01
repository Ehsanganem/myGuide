package com.example.myguidefirebase;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private List<User> usersList;

    public UsersAdapter(List<User> usersList) {
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guide, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = usersList.get(position);
        holder.textViewName.setText(user.getName());
        holder.textViewLocation.setText(user.getLocation().get("country")); // Only country
        holder.textViewLanguages.setText("Languages: " + user.getLanguages().toString());
        holder.textViewServices.setText("Services: " + user.getServices().toString());
        holder.textViewPricePerDay.setText("Price per Day: " + user.getPricePerDay());
        holder.textViewRole.setText("Role: " + capitalizeFirstLetter(user.getRole())); // Dynamic role
        holder.textViewBio.setText(user.getBio());

        // Load profile picture using Glide or similar library
        Glide.with(holder.itemView.getContext())
                .load(user.getIdPhotoUrl())
                .circleCrop() // This will crop the image to a circle
                .into(holder.imageViewProfilePic);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), GuideDetailActivity.class);
            intent.putExtra("selectedGuide", user);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    // Helper method to capitalize the first letter of the role
    private String capitalizeFirstLetter(String role) {
        if (role == null || role.isEmpty()) {
            return role;
        }
        return role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase().replace('_', ' ');
    }



    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName, textViewLocation, textViewLanguages, textViewServices, textViewPricePerDay, textViewRole, textViewBio;
        public ImageView imageViewProfilePic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            textViewLanguages = itemView.findViewById(R.id.textViewLanguages);
            textViewServices = itemView.findViewById(R.id.textViewServices);
            textViewPricePerDay = itemView.findViewById(R.id.textViewPricePerDay);
            textViewRole = itemView.findViewById(R.id.textViewRole);
            textViewBio = itemView.findViewById(R.id.textViewBio);
            imageViewProfilePic = itemView.findViewById(R.id.imageViewProfilePic);
        }
    }
}
