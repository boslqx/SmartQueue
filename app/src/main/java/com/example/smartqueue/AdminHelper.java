package com.example.smartqueue;

import android.content.Context;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminHelper {

    private static final String TAG = "AdminHelper";

    public interface AdminCheckCallback {
        void onResult(boolean isAdmin);
    }

    /**
     * Check if the current user is an admin
     * Checks Firestore for a user document with is_admin = true
     */
    public static void checkIfAdmin(Context context, AdminCheckCallback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.d(TAG, "No user logged in");
            callback.onResult(false);
            return;
        }

        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isAdmin = documentSnapshot.getBoolean("is_admin");
                        boolean result = isAdmin != null && isAdmin;
                        Log.d(TAG, "User " + userId + " admin status: " + result);
                        callback.onResult(result);
                    } else {
                        Log.d(TAG, "User document does not exist");
                        callback.onResult(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking admin status: " + e.getMessage(), e);
                    callback.onResult(false);
                });
    }

    /**
     * Alternative: Check if user email is in admin list
     * Use this if you want to hardcode admin emails
     */
    public static void checkIfAdminByEmail(Context context, AdminCheckCallback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            callback.onResult(false);
            return;
        }

        String email = currentUser.getEmail();

        // List of admin emails (you can modify this)
        String[] adminEmails = {
                "admin@smartqueue.com",
                "boss@smartqueue.com",
                // Add more admin emails here
        };

        for (String adminEmail : adminEmails) {
            if (adminEmail.equalsIgnoreCase(email)) {
                Log.d(TAG, "User " + email + " is admin (email match)");
                callback.onResult(true);
                return;
            }
        }

        Log.d(TAG, "User " + email + " is not admin");
        callback.onResult(false);
    }

    /**
     * Make a user an admin (for testing or initial setup)
     * Call this once to set up your admin account
     */
    public static void makeUserAdmin(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .update("is_admin", true)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User " + userId + " is now an admin");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error making user admin: " + e.getMessage(), e);
                });
    }

    /**
     * Remove admin privileges
     */
    public static void removeAdminPrivileges(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .update("is_admin", false)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Admin privileges removed from user " + userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing admin: " + e.getMessage(), e);
                });
    }
}