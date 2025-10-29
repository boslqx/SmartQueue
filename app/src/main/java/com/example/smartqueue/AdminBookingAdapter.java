package com.example.smartqueue;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore; // ← ADD THIS IMPORT
import java.util.List;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.AdminBookingViewHolder> {

    private Context context;
    private List<BookingModel> bookingList;
    private FirebaseFirestore db;

    public AdminBookingAdapter(Context context, List<BookingModel> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public AdminBookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_booking, parent, false);
        return new AdminBookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminBookingViewHolder holder, int position) {
        BookingModel booking = bookingList.get(position);

        // Set all the existing data (same as your friend's adapter)
        holder.tvServiceName.setText(booking.getServiceName());
        holder.tvLocation.setText(booking.getLocationId());
        holder.tvDate.setText(booking.getDate());
        holder.tvTimeSlot.setText(booking.getTimeSlot());

        // Set user info if available
        if (booking.getUserName() != null) {
            holder.tvUserName.setText("User: " + booking.getUserName());
        } else {
            holder.tvUserName.setText("User: Unknown");
        }

        // Set status with appropriate color (enhanced for admin)
        holder.tvStatus.setText(booking.getStatus().toUpperCase());
        int statusColor;
        switch (booking.getStatus().toLowerCase()) {
            case "confirmed":
            case "approved":
                statusColor = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            case "cancelled":
            case "rejected":
                statusColor = context.getResources().getColor(android.R.color.holo_red_dark);
                break;
            case "completed":
                statusColor = context.getResources().getColor(android.R.color.darker_gray);
                break;
            case "pending":
                statusColor = context.getResources().getColor(android.R.color.holo_blue_dark);
                break;
            default:
                statusColor = context.getResources().getColor(android.R.color.black);
        }
        holder.tvStatus.setTextColor(statusColor);

        // Set payment info
        if (booking.isPaid()) {
            holder.tvAmount.setText(String.format("RM %.2f", booking.getAmount()));
            holder.tvPaymentStatus.setText("PAID");
            holder.tvPaymentStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvAmount.setText("Free");
            holder.tvPaymentStatus.setText("FREE");
            holder.tvPaymentStatus.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
        }

        // ADMIN-SPECIFIC FEATURES:

        // Show/hide admin buttons based on status
        if ("pending".equalsIgnoreCase(booking.getStatus())) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.GONE);
        } else if ("approved".equalsIgnoreCase(booking.getStatus())) {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.VISIBLE);
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);
        }

        // Approve button
        holder.btnApprove.setOnClickListener(v -> {
            updateBookingStatus(booking.getDocumentId(), "approved", "Booking approved");
        });

        // Reject button
        holder.btnReject.setOnClickListener(v -> {
            updateBookingStatus(booking.getDocumentId(), "rejected", "Booking rejected");
        });

        // Cancel button (for approved bookings)
        holder.btnCancel.setOnClickListener(v -> {
            updateBookingStatus(booking.getDocumentId(), "cancelled", "Booking cancelled");
        });

        // View Details button (same as original)
        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookingDetailsActivity.class);
            intent.putExtra("bookingId", booking.getDocumentId());
            context.startActivity(intent);
        });

        // Highlight card based on status
        if ("cancelled".equalsIgnoreCase(booking.getStatus()) || "rejected".equalsIgnoreCase(booking.getStatus())) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.cardView.setAlpha(0.7f);
        } else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
            holder.cardView.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    // ADMIN FUNCTION: Update booking status
    private void updateBookingStatus(String bookingId, String newStatus, String message) {
        db.collection("bookings").document(bookingId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    // Refresh the data
                    if (context instanceof BookingManagementActivity) {
                        ((BookingManagementActivity) context).loadBookingsDirect();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public static class AdminBookingViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvServiceName, tvLocation, tvDate, tvTimeSlot;
        TextView tvStatus, tvAmount, tvPaymentStatus, tvUserName;
        Button btnViewDetails, btnApprove, btnReject, btnCancel;

        public AdminBookingViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTimeSlot = itemView.findViewById(R.id.tvTimeSlot);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}