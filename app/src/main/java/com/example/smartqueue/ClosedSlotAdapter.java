package com.example.smartqueue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ClosedSlotAdapter extends RecyclerView.Adapter<ClosedSlotAdapter.ViewHolder> {

    private List<ClosedSlotModel> closedSlots;
    private OnDeleteListener deleteListener;

    public interface OnDeleteListener {
        void onDelete(ClosedSlotModel slot);
    }

    public ClosedSlotAdapter(List<ClosedSlotModel> closedSlots, OnDeleteListener deleteListener) {
        this.closedSlots = closedSlots;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_closed_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClosedSlotModel slot = closedSlots.get(position);

        holder.tvDate.setText("📅 " + slot.getDate());
        holder.tvTimeSlot.setText("🕐 " + slot.getTimeSlot());
        holder.tvService.setText("📍 " + slot.getService_type());
        holder.tvReason.setText(slot.getReason() != null && !slot.getReason().isEmpty()
                ? slot.getReason() : "No reason provided");

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(slot);
            }
        });
    }

    @Override
    public int getItemCount() {
        return closedSlots != null ? closedSlots.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvDate, tvTimeSlot, tvService, tvReason;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardClosedSlot);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTimeSlot = itemView.findViewById(R.id.tvTimeSlot);
            tvService = itemView.findViewById(R.id.tvService);
            tvReason = itemView.findViewById(R.id.tvReason);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}