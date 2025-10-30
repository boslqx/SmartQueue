package com.example.smartqueue;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManagementActivity extends AppCompatActivity {

    private static final String TAG = "UserManagement";

    private FirebaseFirestore db;
    private ImageView btnBack;
    private SearchView searchView;
    private RecyclerView recyclerViewUsers;
    private LinearLayout tvEmptyState;
    private TextView tvUserCount;
    private ProgressBar progressBar;

    private UserAdapter adapter;
    private List<UserModel> allUsersList;
    private List<UserModel> filteredUsersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupSearchView();
        loadUsers();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        searchView = findViewById(R.id.searchView);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvUserCount = findViewById(R.id.tvUserCount);
        progressBar = findViewById(R.id.progressBar);

        allUsersList = new ArrayList<>();
        filteredUsersList = new ArrayList<>();

        adapter = new UserAdapter(
                this,
                filteredUsersList,
                this::showUserDetailsDialog,
                this::toggleAdminStatus
        );

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(adapter);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });
    }

    private void loadUsers() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        recyclerViewUsers.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);

        Log.d(TAG, "Loading all users from Firestore...");

        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isFinishing() && !isDestroyed()) {
                        allUsersList.clear();

                        Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " users");

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                UserModel user = new UserModel();
                                user.setUid(document.getId());
                                user.setName(document.getString("name"));
                                user.setEmail(document.getString("email"));
                                user.setSchool(document.getString("school"));

                                // Check both isAdmin field and role field
                                Boolean isAdminField = document.getBoolean("isAdmin");
                                String roleField = document.getString("role");

                                // Set admin status based on either field
                                boolean isAdmin = (isAdminField != null && isAdminField) ||
                                        (roleField != null && roleField.equalsIgnoreCase("admin"));
                                user.setAdmin(isAdmin);

                                // Get timestamps if available
                                user.setCreatedAt(document.getTimestamp("created_at"));
                                user.setUpdatedAt(document.getTimestamp("updated_at"));

                                allUsersList.add(user);

                                Log.d(TAG, "Loaded user: " + user.getName() +
                                        " (" + user.getEmail() + ") - Admin: " + user.isAdmin() +
                                        " - Role: " + roleField);

                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing user: " + e.getMessage(), e);
                            }
                        }

                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }

                        // Display all users initially
                        filterUsers("");

                        // Update user count
                        updateUserCount();

                        Log.d(TAG, "Successfully loaded " + allUsersList.size() + " users");
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Log.e(TAG, "Error loading users: " + e.getMessage(), e);

                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        tvEmptyState.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Error loading users: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void filterUsers(String query) {
        filteredUsersList.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredUsersList.addAll(allUsersList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (UserModel user : allUsersList) {
                if ((user.getName() != null && user.getName().toLowerCase().contains(lowerQuery)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery)) ||
                        (user.getSchool() != null && user.getSchool().toLowerCase().contains(lowerQuery))) {
                    filteredUsersList.add(user);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredUsersList.isEmpty()) {
            recyclerViewUsers.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewUsers.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }

        updateUserCount();
    }

    private void updateUserCount() {
        if (tvUserCount != null) {
            int totalUsers = allUsersList.size();
            int displayedUsers = filteredUsersList.size();

            if (totalUsers == displayedUsers) {
                tvUserCount.setText("Total Users: " + totalUsers);
            } else {
                tvUserCount.setText("Showing " + displayedUsers + " of " + totalUsers + " users");
            }
        }
    }

    private void showUserDetailsDialog(UserModel user) {
        String details = "Name: " + user.getName() + "\n" +
                "Email: " + user.getEmail() + "\n" +
                "School: " + (user.getSchool() != null ? user.getSchool() : "N/A") + "\n" +
                "Role: " + (user.isAdmin() ? "Admin" : "Regular User") + "\n" +
                "User ID: " + user.getUid();

        new AlertDialog.Builder(this)
                .setTitle("User Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private void toggleAdminStatus(UserModel user) {
        String action = user.isAdmin() ? "remove admin privileges from" : "grant admin privileges to";

        new AlertDialog.Builder(this)
                .setTitle(user.isAdmin() ? "Remove Admin" : "Make Admin")
                .setMessage("Are you sure you want to " + action + " " + user.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    updateAdminStatus(user);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateAdminStatus(UserModel user) {
        boolean newAdminStatus = !user.isAdmin();

        Map<String, Object> updates = new HashMap<>();
        updates.put("isAdmin", newAdminStatus);
        // Also update role field to maintain consistency
        updates.put("role", newAdminStatus ? "admin" : "user");

        db.collection("users").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    user.setAdmin(newAdminStatus);
                    adapter.notifyDataSetChanged();

                    String message = newAdminStatus ?
                            user.getName() + " is now an admin" :
                            "Admin privileges removed from " + user.getName();
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "Updated admin status for " + user.getEmail());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating user: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating admin status: " + e.getMessage(), e);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }
}