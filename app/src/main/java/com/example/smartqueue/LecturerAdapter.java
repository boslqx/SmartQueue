package com.example.smartqueue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LecturerAdapter extends RecyclerView.Adapter<LecturerAdapter.LecturerViewHolder> {

    private final List<LecturerModel> lecturers;
    private final OnLecturerClickListener listener;

    public interface OnLecturerClickListener {
        void onLecturerClick(LecturerModel lecturer);
    }

    public LecturerAdapter(List<LecturerModel> lecturers, OnLecturerClickListener listener) {
        this.lecturers = lecturers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LecturerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lecturer, parent, false);
        return new LecturerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LecturerViewHolder holder, int position) {
        LecturerModel lecturer = lecturers.get(position);
        holder.bind(lecturer, listener);
    }

    @Override
    public int getItemCount() {
        return lecturers.size();
    }

    // Remove 'private' modifier - use package-private instead
    static class LecturerViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvLecturerName;
        private final TextView tvLecturerDepartment;
        private final TextView tvLecturerAvailability;

        public LecturerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLecturerName = itemView.findViewById(R.id.tvLecturerName);
            tvLecturerDepartment = itemView.findViewById(R.id.tvLecturerDepartment);
            tvLecturerAvailability = itemView.findViewById(R.id.tvLecturerAvailability);
        }

        public void bind(LecturerModel lecturer, OnLecturerClickListener listener) {
            tvLecturerName.setText(lecturer.getName());
            tvLecturerDepartment.setText(lecturer.getDepartment());

            String availability = "Available: " + lecturer.getFormattedAvailableDays();
            if (!lecturer.hasAvailableHours()) {
                availability = "Fully Booked";
                tvLecturerAvailability.setTextColor(0xFFD32F2F); // Red color
            } else {
                tvLecturerAvailability.setTextColor(0xFF666666); // Default gray color
            }
            tvLecturerAvailability.setText(availability);

            itemView.setOnClickListener(v -> {
                if (lecturer.hasAvailableHours()) {
                    listener.onLecturerClick(lecturer);
                }
            });

            // Disable click if fully booked
            itemView.setEnabled(lecturer.hasAvailableHours());
            itemView.setAlpha(lecturer.hasAvailableHours() ? 1.0f : 0.5f);
        }
    }
}