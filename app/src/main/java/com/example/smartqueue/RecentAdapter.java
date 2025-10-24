package com.example.smartqueue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.BookingViewHolder> {

    private List<BookingModel> bookings;
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(BookingModel booking);
    }

    public RecentAdapter(List<BookingModel> bookings, OnBookingClickListener listener) {
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_activity, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingModel booking = bookings.get(position);

        // Service name and location
        holder.tvServiceName.setText(booking.getServiceName());
        holder.tvLocation.setText(booking.getLocationId());

        // Date and time
        String dateTime = booking.getDate() + " • " + booking.getStartTime() + "-" + booking.getEndTime();
        holder.tvDateTime.setText(dateTime);

        // Status
        holder.tvStatus.setText(booking.getStatus().toUpperCase());

        // Set card color based on status
        int backgroundColor;
        int statusColor;
        switch (booking.getStatus().toLowerCase()) {
            case "confirmed":
                backgroundColor = 0xFFE8F5E9; // Light green
                statusColor = 0xFF4CAF50;
                break;
            case "cancelled":
                backgroundColor = 0xFFFFEBEE; // Light red
                statusColor = 0xFFF44336;
                break;
            case "completed":
                backgroundColor = 0xFFE0E0E0; // Light gray
                statusColor = 0xFF757575;
                break;
            default:
                backgroundColor = 0xFFFFF3E0; // Light orange
                statusColor = 0xFFFF9800;
        }

        holder.cardView.setCardBackgroundColor(backgroundColor);
        holder.tvStatus.setTextColor(statusColor);

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookingClick(booking);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvServiceName, tvLocation, tvDateTime, tvStatus;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardBooking);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}