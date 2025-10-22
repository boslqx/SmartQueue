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
    private int maxDuration;
    private int currentSelectedDuration = 0;

    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(int position);
        void onSelectionChanged(int selectedDuration, int maxDuration);
    }

    public TimeSlotAdapter(List<TimeSlotModel> timeSlots, OnTimeSlotClickListener listener, int maxDuration) {
        this.timeSlots = timeSlots;
        this.listener = listener;
        this.maxDuration = maxDuration;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeSlotModel slot = timeSlots.get(position);

        holder.tvTimeRange.setText(slot.getTimeRange());

        if (!slot.isAvailable()) {
            // Booked/Unavailable slot
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.gray_light));
            holder.tvStatus.setText("Unavailable");
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_dark));
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.cardView.setClickable(false);
            holder.cardView.setAlpha(0.6f);
        } else if (slot.isSelected()) {
            // Selected slot
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.selected_color));
            holder.tvStatus.setText(R.string.status_selected);
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_dark));
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.cardView.setClickable(true);
            holder.cardView.setAlpha(1.0f);
        } else if (currentSelectedDuration >= maxDuration) {
            // Max duration reached - can't select more
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.background));
            holder.tvStatus.setText("Max reached");
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_dark));
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.cardView.setClickable(false);
            holder.cardView.setAlpha(0.5f);
        } else {
            // Available slot
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            holder.tvStatus.setText(R.string.status_available);
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.available_color));
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.cardView.setClickable(true);
            holder.cardView.setAlpha(1.0f);
        }

        holder.cardView.setOnClickListener(v -> {
            if (slot.isAvailable()) {
                listener.onTimeSlotClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    public void updateSelectedDuration(int duration) {
        this.currentSelectedDuration = duration;
        notifyDataSetChanged();
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