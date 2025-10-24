package com.example.smartqueue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {

    private List<AnnouncementModel> announcements;

    public AnnouncementAdapter(List<AnnouncementModel> announcements) {
        this.announcements = announcements;
    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_announcement, parent, false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
        AnnouncementModel announcement = announcements.get(position);

        holder.tvTitle.setText(announcement.getIcon() + " " + announcement.getTitle());
        holder.tvMessage.setText(announcement.getMessage());
        holder.cardView.setCardBackgroundColor(announcement.getBackgroundColor());
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    public static class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle, tvMessage;

        public AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardAnnouncement);
            tvTitle = itemView.findViewById(R.id.tvAnnouncementTitle);
            tvMessage = itemView.findViewById(R.id.tvAnnouncementMessage);
        }
    }
}