package com.example.smartqueue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import java.util.List;

public class CarouselAnnouncementAdapter extends RecyclerView.Adapter<CarouselAnnouncementAdapter.CarouselViewHolder> {

    private List<AnnouncementModel> announcements;
    private ViewPager2 viewPager2;

    public CarouselAnnouncementAdapter(List<AnnouncementModel> announcements, ViewPager2 viewPager2) {
        this.announcements = announcements;
        this.viewPager2 = viewPager2;
    }

    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_announcement_carousel, parent, false);
        return new CarouselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {
        AnnouncementModel announcement = announcements.get(position);

        holder.tvTitle.setText(announcement.getIcon() + " " + announcement.getTitle());
        holder.tvMessage.setText(announcement.getMessage());
        holder.cardView.setCardBackgroundColor(announcement.getBackgroundColor());
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    public static class CarouselViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle, tvMessage;

        public CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardAnnouncement);
            tvTitle = itemView.findViewById(R.id.tvAnnouncementTitle);
            tvMessage = itemView.findViewById(R.id.tvAnnouncementMessage);
        }
    }
}