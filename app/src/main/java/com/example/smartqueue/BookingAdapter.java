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

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private Context context;
    private List<BookingModel> bookingList;

    public BookingAdapter(Context context, List<BookingModel> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingModel booking = bookingList.get(position);

        holder.tvServiceName.setText(booking.getServiceName());
        holder.tvLocation.setText(booking.getLocationId());
        holder.tvDate.setText(booking.getDate());
        holder.tvTimeSlot.setText(booking.getTimeSlot());

        // Get computed status (handles expiry)
        String displayStatus = booking.getComputedStatus();
        holder.tvStatus.setText(displayStatus.toUpperCase());

        // Set status color based on computed status
        int statusColor;
        switch (displayStatus.toLowerCase()) {
            case "confirmed":
                statusColor = context.getResources().getColor(R.color.available_color);
                break;
            case "cancelled":
                statusColor = context.getResources().getColor(R.color.booked_color);
                break;
            case "expired":
                statusColor = context.getResources().getColor(R.color.gray_light);
                break;
            case "completed":
                statusColor = context.getResources().getColor(R.color.gray_light);
                break;
            default:
                statusColor = context.getResources().getColor(R.color.text_dark);
        }
        holder.tvStatus.setTextColor(statusColor);

        // Set payment info
        if (booking.isPaid()) {
            holder.tvAmount.setText(String.format("RM %.2f", booking.getAmount()));
            holder.tvPaymentStatus.setText("PAID");
            holder.tvPaymentStatus.setTextColor(context.getResources().getColor(R.color.available_color));
        } else {
            holder.tvAmount.setText("Free");
            holder.tvPaymentStatus.setText("FREE");
            holder.tvPaymentStatus.setTextColor(context.getResources().getColor(R.color.primary));
        }

        // View Details button
        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookingDetailsActivity.class);
            intent.putExtra("bookingId", booking.getDocumentId());
            context.startActivity(intent);
        });

        // Highlight card based on status
        if ("cancelled".equalsIgnoreCase(displayStatus) || "expired".equalsIgnoreCase(displayStatus)) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.gray_light));
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

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvServiceName, tvLocation, tvDate, tvTimeSlot;
        TextView tvStatus, tvAmount, tvPaymentStatus;
        Button btnViewDetails;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTimeSlot = itemView.findViewById(R.id.tvTimeSlot);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}