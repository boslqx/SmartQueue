// TimeSlotAdapter.java
package com.example.smartqueue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {

    private List<TimeSlotModel> timeSlots;
    private OnTimeSlotClickListener listener;

    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(int position);
    }

    public TimeSlotAdapter(List<TimeSlotModel> timeSlots, OnTimeSlotClickListener listener) {
        this.timeSlots = timeSlots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }

    // In your onBindViewHolder method, update the color logic:
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeSlotModel slot = timeSlots.get(position);

        holder.tvTimeRange.setText(slot.getTimeRange());

        if (!slot.isAvailable()) {
            // Booked slot - using your accent color
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_light));
            holder.tvStatus.setText(R.string.status_booked);
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.booked_color));
            holder.cardView.setClickable(false);
        } else if (slot.isSelected()) {
            // Selected slot - using your primary color
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.selected_color));
            holder.tvStatus.setText(R.string.status_selected);
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
            holder.cardView.setClickable(true);
        } else {
            // Available slot - using your background color
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.background));
            holder.tvStatus.setText(R.string.status_available);
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.available_color));
            holder.cardView.setClickable(true);
        }

        holder.cardView.setOnClickListener(v -> {
            if (slot.isAvailable() && !slot.isSelected()) {
                listener.onTimeSlotClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTimeRange;
        TextView tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardTimeSlot);
            tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}