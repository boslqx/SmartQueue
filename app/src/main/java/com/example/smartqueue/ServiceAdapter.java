package com.example.smartqueue;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<Service> serviceList;

    public ServiceAdapter(List<Service> serviceList) {
        this.serviceList = serviceList;
    }

    @Override
    public ServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);
        holder.tvServiceName.setText(service.getName());
        holder.tvServiceDescription.setText(service.getDescription());
        holder.tvServiceLocation.setText(service.getLocation());
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        public TextView tvServiceName, tvServiceDescription, tvServiceLocation;

        public ServiceViewHolder(View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceDescription = itemView.findViewById(R.id.tvServiceDescription);
            tvServiceLocation = itemView.findViewById(R.id.tvServiceLocation);
        }
    }
}