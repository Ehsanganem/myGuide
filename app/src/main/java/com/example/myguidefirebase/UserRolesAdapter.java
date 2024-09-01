package com.example.myguidefirebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class UserRolesAdapter extends RecyclerView.Adapter<UserRolesAdapter.ViewHolder> {

    private List<User> userList;
    private Context context;
    private FirebaseFirestore db;

    public UserRolesAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public UserRolesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_role, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserRolesAdapter.ViewHolder holder, int position) {
        User user = userList.get(position);

        holder.textViewUserName.setText(user.getName());
        holder.textViewUserRole.setText(capitalizeFirstLetter(user.getRole()));

        if ("admin".equalsIgnoreCase(user.getRole())) {
            holder.buttonPromote.setText("Already Admin");
            holder.buttonPromote.setEnabled(false);
        } else {
            holder.buttonPromote.setText("Promote to Admin");
            holder.buttonPromote.setEnabled(true);
            holder.buttonPromote.setOnClickListener(v -> promoteUserToAdmin(user, holder));
        }

        holder.buttonDemote.setVisibility(View.GONE); // Hide the demote button if you don't need it
    }

    private void promoteUserToAdmin(User user, ViewHolder holder) {
        db.collection("users")
                .document(user.getUserId())
                .update("role", "admin")
                .addOnSuccessListener(aVoid -> {
                    user.setRole("admin");
                    notifyItemChanged(holder.getAdapterPosition());
                    Toast.makeText(context, user.getName() + " has been promoted to Admin.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to promote " + user.getName(), Toast.LENGTH_SHORT).show();
                });
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUserName, textViewUserRole;
        Button buttonPromote, buttonDemote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            textViewUserRole = itemView.findViewById(R.id.textViewUserRole);
            buttonPromote = itemView.findViewById(R.id.buttonPromote);
            buttonDemote = itemView.findViewById(R.id.buttonDemote);
        }
    }
}
