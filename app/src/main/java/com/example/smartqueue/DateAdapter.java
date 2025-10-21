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

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    private List<DateItemModel> dateList;
    private OnDateClickListener listener;
    private int selectedPosition = 0;

    public interface OnDateClickListener {
        void onDateClick(String date, int position);
    }

    public DateAdapter(List<DateItemModel> dateList, OnDateClickListener listener) {
        this.dateList = dateList;
        this.listener = listener;
        // Set first item as selected by default
        if (!dateList.isEmpty()) {
            dateList.get(0).setSelected(true);
        }
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        DateItemModel dateItem = dateList.get(position);

        holder.tvDayOfWeek.setText(dateItem.getDayOfWeek());
        holder.tvDayOfMonth.setText(dateItem.getDayOfMonth());

        // Show "Today" label for today's date
        if (dateItem.isToday()) {
            holder.tvToday.setVisibility(View.VISIBLE);
        } else {
            holder.tvToday.setVisibility(View.GONE);
        }

        // Apply selection styling
        if (dateItem.isSelected()) {
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
            holder.tvDayOfWeek.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_dark));
            holder.tvDayOfMonth.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_dark));
            holder.tvToday.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_dark));
            holder.cardView.setCardElevation(8f);
        } else {
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            holder.tvDayOfWeek.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_dark));
            holder.tvDayOfMonth.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_dark));
            holder.tvToday.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
            holder.cardView.setCardElevation(4f);
        }

        holder.cardView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }

            DateItemModel clickedDate = dateList.get(adapterPosition);

            // Update selection
            int previousPosition = selectedPosition;
            selectedPosition = adapterPosition;

            // Deselect previous
            if (previousPosition >= 0 && previousPosition < dateList.size()) {
                dateList.get(previousPosition).setSelected(false);
                notifyItemChanged(previousPosition);
            }

            // Select new
            clickedDate.setSelected(true);
            notifyItemChanged(adapterPosition);

            if (listener != null) {
                listener.onDateClick(clickedDate.getDate(), adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public void setSelectedPosition(int position) {
        if (position >= 0 && position < dateList.size()) {
            int previousPosition = selectedPosition;
            selectedPosition = position;

            if (previousPosition >= 0 && previousPosition < dateList.size()) {
                dateList.get(previousPosition).setSelected(false);
                notifyItemChanged(previousPosition);
            }

            dateList.get(position).setSelected(true);
            notifyItemChanged(position);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvDayOfWeek;
        TextView tvDayOfMonth;
        TextView tvToday;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            tvDayOfMonth = itemView.findViewById(R.id.tvDayOfMonth);
            tvToday = itemView.findViewById(R.id.tvToday);
        }
    }
}