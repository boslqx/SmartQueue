package com.example.smartqueue;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<UserModel> userList;
    private OnUserClickListener detailsListener;
    private OnUserActionListener actionListener;

    public interface OnUserClickListener {
        void onUserClick(UserModel user);
    }

    public interface OnUserActionListener {
        void onToggleAdmin(UserModel user);
    }

    public UserAdapter(Context context, List<UserModel> userList,
                       OnUserClickListener detailsListener,
                       OnUserActionListener actionListener) {
        this.context = context;
        this.userList = userList;
        this.detailsListener = detailsListener;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);

        // Set user initials
        holder.tvInitials.setText(user.getInitials());

        // Set user name
        holder.tvUserName.setText(user.getDisplayName());

        // Set user email
        holder.tvUserEmail.setText(user.getEmail());

        // Set school
        if (user.getSchool() != null && !user.getSchool().isEmpty()) {
            holder.tvSchool.setText(user.getSchool());
            holder.tvSchool.setVisibility(View.VISIBLE);
        } else {
            holder.tvSchool.setVisibility(View.GONE);
        }

        // Set role badge
        if (user.isAdmin()) {
            holder.tvRole.setText("ADMIN");
            holder.tvRole.setBackgroundColor(context.getResources().getColor(R.color.accent_light));
            holder.tvRole.setTextColor(context.getResources().getColor(R.color.booked_color));
            holder.ivAdminIcon.setVisibility(View.VISIBLE);
        } else {
            holder.tvRole.setText("USER");
            holder.tvRole.setBackgroundColor(context.getResources().getColor(R.color.background));
            holder.tvRole.setTextColor(context.getResources().getColor(R.color.primary));
            holder.ivAdminIcon.setVisibility(View.GONE);
        }

        // Set initials background color based on user type
        int backgroundColor = user.isAdmin()
                ? context.getResources().getColor(R.color.accent_light)
                : context.getResources().getColor(R.color.primary);
        holder.cardInitials.setCardBackgroundColor(backgroundColor);

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (detailsListener != null) {
                detailsListener.onUserClick(user);
            }
        });

        holder.btnToggleAdmin.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onToggleAdmin(user);
            }
        });

        // Update toggle button text
        holder.btnToggleAdmin.setText(user.isAdmin() ? "Remove Admin" : "Make Admin");
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CardView cardInitials;
        TextView tvInitials, tvUserName, tvUserEmail, tvSchool, tvRole;
        ImageView ivAdminIcon;
        TextView btnToggleAdmin;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            cardInitials = itemView.findViewById(R.id.cardInitials);
            tvInitials = itemView.findViewById(R.id.tvInitials);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvSchool = itemView.findViewById(R.id.tvSchool);
            tvRole = itemView.findViewById(R.id.tvRole);
            ivAdminIcon = itemView.findViewById(R.id.ivAdminIcon);
            btnToggleAdmin = itemView.findViewById(R.id.btnToggleAdmin);
        }
    }
}