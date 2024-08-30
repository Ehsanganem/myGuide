package com.example.myguidefirebase;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookingList;
    private FirebaseFirestore db;
    private OnCancelClickListener cancelClickListener;
    private Context context;

    public BookingAdapter(List<Booking> bookingList, OnCancelClickListener cancelClickListener, Context context) {
        this.bookingList = bookingList;
        this.db = FirebaseFirestore.getInstance();
        this.cancelClickListener = cancelClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        // Fetch tourist name
        db.collection("users").document(booking.getTouristId()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String touristName = documentSnapshot.getString("name");
                holder.textViewTouristName.setText("Tourist: " + touristName);
            } else {
                holder.textViewTouristName.setText("Tourist: Unknown");
            }
        });

        // Fetch guide name
        db.collection("users").document(booking.getGuideId()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String guideName = documentSnapshot.getString("name");
                holder.textViewGuideName.setText("Guide: " + guideName);
            } else {
                holder.textViewGuideName.setText("Guide: Unknown");
            }
        });

        holder.textViewBookingId.setText("Booking ID: " + booking.getBookingId());
        holder.textViewStatus.setText("Status: " + booking.getStatus());

        // Format dates for display
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startDate = dateFormat.format(booking.getStartDate());
        String endDate = dateFormat.format(booking.getEndDate());

        holder.textViewStartDate.setText("Start Date: " + startDate);
        holder.textViewEndDate.setText("End Date: " + endDate);

        holder.textViewTotalCost.setText("Total Cost: $" + booking.getTotalCost());

        holder.buttonCancelBooking.setOnClickListener(v -> {
            if (cancelClickListener != null) {
                // Show confirmation dialog before canceling
                new AlertDialog.Builder(context)
                        .setTitle("Cancel Booking")
                        .setMessage("Are you sure you want to cancel this booking?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            cancelClickListener.onCancelClick(booking);
                            holder.textViewStatus.setText("Status: canceled");
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        holder.imageViewDeleteBooking.setOnClickListener(v -> {
            // Show confirmation dialog before deleting
            new AlertDialog.Builder(context)
                    .setTitle("Delete Booking")
                    .setMessage("Are you sure you want to delete this booking?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        db.collection("bookings").document(booking.getBookingId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    bookingList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, bookingList.size());

                                    sendNotificationToGuideAndTourist(booking, "Booking Deleted", "Your booking with " + booking.getGuideName() + " from " + booking.getStartDate() + " to " + booking.getEndDate() + " has been deleted.");
                                    Toast.makeText(context, "Booking deleted", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete booking", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTouristName, textViewGuideName, textViewBookingId, textViewStatus, textViewStartDate, textViewEndDate, textViewTotalCost;
        Button buttonCancelBooking;
        ImageView imageViewDeleteBooking;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTouristName = itemView.findViewById(R.id.textViewTouristName);
            textViewGuideName = itemView.findViewById(R.id.textViewGuideName);
            textViewBookingId = itemView.findViewById(R.id.textViewBookingId);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewStartDate = itemView.findViewById(R.id.textViewStartDate);
            textViewEndDate = itemView.findViewById(R.id.textViewEndDate);
            textViewTotalCost = itemView.findViewById(R.id.textViewTotalCost);
            buttonCancelBooking = itemView.findViewById(R.id.buttonCancelBooking);
            imageViewDeleteBooking = itemView.findViewById(R.id.imageViewDeleteBooking);
        }
    }

    private void sendNotificationToGuideAndTourist(Booking booking, String title, String message) {
        MyFirebaseMessagingService messagingService = new MyFirebaseMessagingService();
        messagingService.sendNotification(context, title, message);

        // Update Firebase Firestore with the notification for both guide and tourist
        addNotificationToFirebase(booking.getTouristId(), title, message);
        addNotificationToFirebase(booking.getGuideId(), title, message);
    }

    private void addNotificationToFirebase(String userId, String title, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("message", message);
        notification.put("isRead", false);
        notification.put("timestamp", new Date());

        db.collection("notifications")
                .document(userId)
                .collection("userNotifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    // Notification successfully added to Firestore
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to add notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public interface OnCancelClickListener {
        void onCancelClick(Booking booking);
    }
}
