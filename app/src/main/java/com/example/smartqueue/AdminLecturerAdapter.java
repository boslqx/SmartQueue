package com.example.smartqueue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminLecturerAdapter extends RecyclerView.Adapter<AdminLecturerAdapter.LecturerViewHolder> {

    private final List<LecturerModel> lecturers;
    private final OnLecturerActionListener listener;

    public interface OnLecturerActionListener {
        void onEdit(LecturerModel lecturer);
        void onDelete(LecturerModel lecturer);
    }

    public AdminLecturerAdapter(List<LecturerModel> lecturers, OnLecturerActionListener listener) {
        this.lecturers = lecturers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LecturerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_lecturer, parent, false);
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

    static class LecturerViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView tvName;
        private final TextView tvEmail;
        private final TextView tvDepartment;
        private final TextView tvOffice;
        private final TextView tvAvailability;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        public LecturerViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardLecturer);
            tvName = itemView.findViewById(R.id.tvLecturerName);
            tvEmail = itemView.findViewById(R.id.tvLecturerEmail);
            tvDepartment = itemView.findViewById(R.id.tvLecturerDepartment);
            tvOffice = itemView.findViewById(R.id.tvLecturerOffice);
            tvAvailability = itemView.findViewById(R.id.tvLecturerAvailability);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(LecturerModel lecturer, OnLecturerActionListener listener) {
            tvName.setText(lecturer.getName());
            tvEmail.setText(lecturer.getEmail());
            tvDepartment.setText(lecturer.getDepartment());
            tvOffice.setText(lecturer.getOffice_location());

            // Show availability status
            String availabilityText = lecturer.getAvailabilityStatus() + " • " +
                    lecturer.getFormattedAvailableDays();
            tvAvailability.setText(availabilityText);

            btnEdit.setOnClickListener(v -> listener.onEdit(lecturer));
            btnDelete.setOnClickListener(v -> listener.onDelete(lecturer));
        }
    }
}