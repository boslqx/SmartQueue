package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SelectSlotActivity extends AppCompatActivity {

    private static final String TAG = "SelectSlotActivity";
    private FirebaseFirestore db;
    private String serviceType;
    private ServiceModel serviceModel;

    private TextView tvServiceName, tvAvailableTime, tvMaxDuration, tvPrice;
    private FrameLayout layoutContainer;
    private View priceContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.select_slot_activity);
            Log.d(TAG, "Layout inflated successfully");

            // Check if user is authenticated
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "User not authenticated");
                finish();
                return;
            }

            // Initialize Firestore
            db = FirebaseFirestore.getInstance();

            // Get service type from intent
            serviceType = getIntent().getStringExtra("serviceType");
            Log.d(TAG, "Service Type: " + serviceType);

            if (serviceType == null) {
                Toast.makeText(this, "Error: No service selected", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize views
            tvServiceName = findViewById(R.id.tvServiceName);
            tvAvailableTime = findViewById(R.id.tvAvailableTime);
            tvMaxDuration = findViewById(R.id.tvMaxDuration);
            tvPrice = findViewById(R.id.tvPrice);
            priceContainer = findViewById(R.id.priceContainer);
            layoutContainer = findViewById(R.id.layoutContainer);
            ImageView btnBack = findViewById(R.id.btnBack);

            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }

            // Load service data from Firestore
            loadServiceData();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadServiceData() {
        Log.d(TAG, "Loading service data for: " + serviceType);

        db.collection("services")
                .document(serviceType)
                .get()
                .addOnSuccessListener(document -> {
                    Log.d(TAG, "Document exists: " + document.exists());

                    if (document.exists()) {
                        Log.d(TAG, "Document data: " + document.getData());
                        Log.d(TAG, "Layout type from document: " + document.getString("layout_type"));
                    }

                    if (document.exists()) {
                        serviceModel = document.toObject(ServiceModel.class);
                        if (serviceModel != null) {
                            Log.d(TAG, "Service Model loaded: " + serviceModel.getName());
                            Log.d(TAG, "Layout type from model: " + serviceModel.getLayout_type());
                            displayServiceInfo();
                            renderLayout();
                        } else {
                            Log.e(TAG, "Service Model is null");
                            Toast.makeText(this, "Error: Service data is null", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Log.e(TAG, "Document does not exist");
                        Toast.makeText(this, "Service not found in database", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading service: " + e.getMessage(), e);
                    Toast.makeText(this, "Error loading service: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayServiceInfo() {
        try {
            tvServiceName.setText(serviceModel.getName());
            tvAvailableTime.setText("Available: " + serviceModel.getAvailable_from() +
                    " - " + serviceModel.getAvailable_to());

            String durationText = "Max Duration: " + serviceModel.getMax_duration() +
                    (serviceModel.getMax_duration() == 1 ? " hour" : " hours");
            tvMaxDuration.setText(durationText);

            if (serviceModel.isIs_paid()) {
                priceContainer.setVisibility(View.VISIBLE);
                tvPrice.setText("Price: RM " +
                        String.format(Locale.getDefault(), "%.2f", serviceModel.getPrice()));
            } else {
                priceContainer.setVisibility(View.VISIBLE);
                tvPrice.setText("Price: Free");
            }

            Log.d(TAG, "Service info displayed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error displaying service info: " + e.getMessage(), e);
        }
    }

    private void renderLayout() {
        try {
            String layoutType = serviceModel.getLayout_type();
            Log.d(TAG, "Rendering layout type: " + layoutType);

            switch (layoutType) {
                case "room_map":
                    inflateDiscussionRoomLayout();
                    break;
                case "vertical_horizontal":
                    inflatePoolTableLayout();
                    break;
                case "side_by_side":
                    inflatePingPongLayout();
                    break;
                case "single_layout":
                    inflateSingleLayout();
                    break;
                case "lecturer_list":
                    inflateLecturerListLayout();
                    break;
                default:
                    Log.e(TAG, "Unknown layout type: " + layoutType);
                    Toast.makeText(this, "Unknown layout type: " + layoutType, Toast.LENGTH_SHORT).show();
                    finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error rendering layout: " + e.getMessage(), e);
            Toast.makeText(this, "Error rendering layout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void inflateDiscussionRoomLayout() {
        try {
            Log.d(TAG, "Attempting to inflate discussion room layout...");

            View layout = LayoutInflater.from(this)
                    .inflate(R.layout.layout_discussion_room, layoutContainer, false);

            Log.d(TAG, "Layout inflated, adding to container...");
            layoutContainer.removeAllViews();
            layoutContainer.addView(layout);

            Log.d(TAG, "Finding room views...");

            // Setup room click listeners
            CardView roomLarge1 = layout.findViewById(R.id.roomLarge1);
            CardView roomLarge2 = layout.findViewById(R.id.roomLarge2);
            CardView roomSmall1 = layout.findViewById(R.id.roomSmall1);
            CardView roomSmall2 = layout.findViewById(R.id.roomSmall2);
            CardView roomSmall3 = layout.findViewById(R.id.roomSmall3);

            if (roomLarge1 == null || roomLarge2 == null || roomSmall1 == null ||
                    roomSmall2 == null || roomSmall3 == null) {
                Log.e(TAG, "One or more room CardViews are null!");
                Toast.makeText(this, "Error: Room views not found in layout", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Setting up click listeners...");
            roomLarge1.setOnClickListener(v -> navigateToTimeSlots("Room L1", "Large"));
            roomLarge2.setOnClickListener(v -> navigateToTimeSlots("Room L2", "Large"));
            roomSmall1.setOnClickListener(v -> navigateToTimeSlots("Room S1", "Small"));
            roomSmall2.setOnClickListener(v -> navigateToTimeSlots("Room S2", "Small"));
            roomSmall3.setOnClickListener(v -> navigateToTimeSlots("Room S3", "Small"));

            Log.d(TAG, "Discussion room layout inflated successfully!");
        } catch (Exception e) {
            Log.e(TAG, "Error inflating discussion room layout: " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(this, "Error loading discussion room: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void inflatePoolTableLayout() {
        try {
            View layout = LayoutInflater.from(this)
                    .inflate(R.layout.layout_pool_table, layoutContainer, false);
            layoutContainer.removeAllViews();
            layoutContainer.addView(layout);

            CardView poolTable1 = layout.findViewById(R.id.poolTable1);
            CardView poolTable2 = layout.findViewById(R.id.poolTable2);

            poolTable1.setOnClickListener(v -> navigateToTimeSlots("Pool Table 1", null));
            poolTable2.setOnClickListener(v -> navigateToTimeSlots("Pool Table 2", null));

            Log.d(TAG, "Pool table layout inflated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error inflating pool table layout: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading pool table layout", Toast.LENGTH_SHORT).show();
        }
    }

    private void inflatePingPongLayout() {
        try {
            View layout = LayoutInflater.from(this)
                    .inflate(R.layout.layout_ping_pong, layoutContainer, false);
            layoutContainer.removeAllViews();
            layoutContainer.addView(layout);

            CardView pingPongTable1 = layout.findViewById(R.id.pingPongTable1);
            CardView pingPongTable2 = layout.findViewById(R.id.pingPongTable2);

            pingPongTable1.setOnClickListener(v -> navigateToTimeSlots("Table 1", null));
            pingPongTable2.setOnClickListener(v -> navigateToTimeSlots("Table 2", null));

            Log.d(TAG, "Ping pong layout inflated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error inflating ping pong layout: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading ping pong layout", Toast.LENGTH_SHORT).show();
        }
    }

    private void inflateSingleLayout() {
        try {
            View layout = LayoutInflater.from(this)
                    .inflate(R.layout.layout_single, layoutContainer, false);
            layoutContainer.removeAllViews();
            layoutContainer.addView(layout);

            TextView tvSingleTitle = layout.findViewById(R.id.tvSingleTitle);
            TextView tvSingleName = layout.findViewById(R.id.tvSingleName);
            ImageView ivSingleIcon = layout.findViewById(R.id.ivSingleIcon);
            CardView singleLocation = layout.findViewById(R.id.singleLocation);

            // Set appropriate icon and text based on service type
            if (serviceType.equals("music_room")) {
                ivSingleIcon.setImageResource(R.drawable.ic_musicroom);
                tvSingleName.setText("Music Room");
                tvSingleTitle.setText("Music Room Available");
                Log.d(TAG, "Loaded Music Room layout");
            } else {
                // Fallback for any other single layout service
                tvSingleName.setText(serviceModel.getName());
                tvSingleTitle.setText(serviceModel.getName() + " Available");
                Log.d(TAG, "Loaded generic single layout for: " + serviceType);
            }

            singleLocation.setOnClickListener(v ->
                    navigateToTimeSlots(serviceModel.getName(), null));

            Log.d(TAG, "Single layout inflated successfully for: " + serviceType);
        } catch (Exception e) {
            Log.e(TAG, "Error inflating single layout: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading layout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void inflateLecturerListLayout() {
        try {
            View layout = LayoutInflater.from(this)
                    .inflate(R.layout.layout_lecturer_list, layoutContainer, false);
            layoutContainer.removeAllViews();
            layoutContainer.addView(layout);

            RecyclerView rvLecturers = layout.findViewById(R.id.rvLecturers);
            rvLecturers.setLayoutManager(new LinearLayoutManager(this));

            // Load lecturers from Firestore
            loadLecturers(rvLecturers);

            Log.d(TAG, "Lecturer list layout inflated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error inflating lecturer list layout: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading lecturer list", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLecturers(RecyclerView recyclerView) {
        db.collection("lecturers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LecturerModel> lecturers = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        LecturerModel lecturer = document.toObject(LecturerModel.class);
                        lecturer.setId(document.getId());
                        lecturers.add(lecturer);
                    }

                    Log.d(TAG, "Loaded " + lecturers.size() + " lecturers");

                    if (lecturers.isEmpty()) {
                        Toast.makeText(this, "No lecturers available", Toast.LENGTH_SHORT).show();
                    } else {
                        LecturerAdapter adapter = new LecturerAdapter(lecturers,
                                lecturer -> navigateToTimeSlots(lecturer.getName(),
                                        lecturer.getId()));
                        recyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading lecturers: " + e.getMessage(), e);
                    Toast.makeText(this, "Error loading lecturers: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToTimeSlots(String locationId, String extraInfo) {
        Log.d(TAG, "Navigation to time slots: " + locationId);

        Intent intent = new Intent(SelectSlotActivity.this, TimeSlotActivity.class);
        intent.putExtra("serviceType", serviceType);
        intent.putExtra("serviceName", serviceModel.getName());
        intent.putExtra("locationId", locationId);
        intent.putExtra("extraInfo", extraInfo);
        intent.putExtra("availableFrom", serviceModel.getAvailable_from());
        intent.putExtra("availableTo", serviceModel.getAvailable_to());
        intent.putExtra("maxDuration", serviceModel.getMax_duration());
        intent.putExtra("isPaid", serviceModel.isIs_paid());
        intent.putExtra("price", serviceModel.getPrice());
        startActivity(intent);

    }
}