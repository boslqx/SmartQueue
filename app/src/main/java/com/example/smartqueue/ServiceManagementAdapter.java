package com.example.smartqueue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ServiceManagementAdapter extends RecyclerView.Adapter<ServiceManagementAdapter.ViewHolder> {

    private List<ServiceModel> services;
    private OnServiceActionListener toggleListener;
    private OnServiceActionListener detailsListener;

    public interface OnServiceActionListener {
        void onAction(ServiceModel service);
    }

    public ServiceManagementAdapter(List<ServiceModel> services,
                                    OnServiceActionListener toggleListener,
                                    OnServiceActionListener detailsListener) {
        this.services = services;
        this.toggleListener = toggleListener;
        this.detailsListener = detailsListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_management, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceModel service = services.get(position);

        holder.tvServiceName.setText(service.getName());
        holder.tvServiceInfo.setText(service.getAvailable_from() + " - " + service.getAvailable_to() +
                " • Max " + service.getMax_duration() + "h");
        holder.tvPrice.setText(service.isIs_paid() ? "RM " + String.format("%.2f", service.getPrice()) : "Free");

        holder.switchEnabled.setChecked(service.isIs_paid());

        holder.switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() && toggleListener != null) {
                toggleListener.onAction(service);
            }
        });

        holder.cardView.setOnClickListener(v -> {
            if (detailsListener != null) {
                detailsListener.onAction(service);
            }
        });
    }

    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvServiceName, tvServiceInfo, tvPrice;
        Switch switchEnabled;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardService);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceInfo = itemView.findViewById(R.id.tvServiceInfo);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            switchEnabled = itemView.findViewById(R.id.switchEnabled);
        }
    }
}