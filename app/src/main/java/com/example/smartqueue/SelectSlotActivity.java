package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
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

    private FirebaseFirestore db;
    private String serviceType;
    private ServiceModel serviceModel;

    private TextView tvServiceName, tvAvailableTime, tvMaxDuration, tvPrice;
    private FrameLayout layoutContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_slot_activity);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get service type from intent
        serviceType = getIntent().getStringExtra("serviceType");
        if (serviceType == null) {
            Toast.makeText(this, R.string.error_no_service_selected, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        tvServiceName = findViewById(R.id.tvServiceName);
        tvAvailableTime = findViewById(R.id.tvAvailableTime);
        tvMaxDuration = findViewById(R.id.tvMaxDuration);
        tvPrice = findViewById(R.id.tvPrice);
        layoutContainer = findViewById(R.id.layoutContainer);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // Load service data from Firestore
        loadServiceData();
    }

    private void loadServiceData() {
        db.collection("services")
                .document(serviceType)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        serviceModel = document.toObject(ServiceModel.class);
                        if (serviceModel != null) {
                            displayServiceInfo();
                            renderLayout();
                        }
                    } else {
                        Toast.makeText(this, R.string.service_not_found, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, getString(R.string.error_loading_service, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayServiceInfo() {
        tvServiceName.setText(serviceModel.getName());
        tvAvailableTime.setText(getString(R.string.available_time_format,
                serviceModel.getAvailable_from(), serviceModel.getAvailable_to()));

        String durationText = getString(R.string.max_duration_hours, serviceModel.getMax_duration());
        tvMaxDuration.setText(durationText);

        if (serviceModel.isIs_paid()) {
            tvPrice.setVisibility(View.VISIBLE);
            tvPrice.setText(getString(R.string.price_format,
                    String.format(Locale.getDefault(), "%.2f", serviceModel.getPrice())));
        } else {
            tvPrice.setVisibility(View.VISIBLE);
            tvPrice.setText(R.string.price_free);
        }
    }

    private void renderLayout() {
        String layoutType = serviceModel.getLayout_type();

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
                Toast.makeText(this, R.string.unknown_layout_type, Toast.LENGTH_SHORT).show();
                finish();
        }
    }

    private void inflateDiscussionRoomLayout() {
        View layout = LayoutInflater.from(this)
                .inflate(R.layout.layout_discussion_room, layoutContainer, false);
        layoutContainer.removeAllViews();
        layoutContainer.addView(layout);

        // Setup room click listeners
        CardView roomLarge1 = layout.findViewById(R.id.roomLarge1);
        CardView roomLarge2 = layout.findViewById(R.id.roomLarge2);
        CardView roomSmall1 = layout.findViewById(R.id.roomSmall1);
        CardView roomSmall2 = layout.findViewById(R.id.roomSmall2);
        CardView roomSmall3 = layout.findViewById(R.id.roomSmall3);

        roomLarge1.setOnClickListener(v -> navigateToTimeSlots("Room L1", "Large"));
        roomLarge2.setOnClickListener(v -> navigateToTimeSlots("Room L2", "Large"));
        roomSmall1.setOnClickListener(v -> navigateToTimeSlots("Room S1", "Small"));
        roomSmall2.setOnClickListener(v -> navigateToTimeSlots("Room S2", "Small"));
        roomSmall3.setOnClickListener(v -> navigateToTimeSlots("Room S3", "Small"));
    }

    private void inflatePoolTableLayout() {
        View layout = LayoutInflater.from(this)
                .inflate(R.layout.layout_pool_table, layoutContainer, false);
        layoutContainer.removeAllViews();
        layoutContainer.addView(layout);

        CardView poolTable1 = layout.findViewById(R.id.poolTable1);
        CardView poolTable2 = layout.findViewById(R.id.poolTable2);

        poolTable1.setOnClickListener(v -> navigateToTimeSlots("Pool Table 1", null));
        poolTable2.setOnClickListener(v -> navigateToTimeSlots("Pool Table 2", null));
    }

    private void inflatePingPongLayout() {
        View layout = LayoutInflater.from(this)
                .inflate(R.layout.layout_ping_pong, layoutContainer, false);
        layoutContainer.removeAllViews();
        layoutContainer.addView(layout);

        CardView pingPongTable1 = layout.findViewById(R.id.pingPongTable1);
        CardView pingPongTable2 = layout.findViewById(R.id.pingPongTable2);

        pingPongTable1.setOnClickListener(v -> navigateToTimeSlots("Table 1", null));
        pingPongTable2.setOnClickListener(v -> navigateToTimeSlots("Table 2", null));
    }

    private void inflateSingleLayout() {
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
            tvSingleName.setText(R.string.music_room);
            tvSingleTitle.setText(R.string.music_room_available);
        } else if (serviceType.equals("multipurpose_court")) {
            ivSingleIcon.setImageResource(R.drawable.ic_field);
            tvSingleName.setText(R.string.multipurpose_court);
            tvSingleTitle.setText(R.string.court_available);
        }

        singleLocation.setOnClickListener(v ->
                navigateToTimeSlots(serviceModel.getName(), null));
    }

    private void inflateLecturerListLayout() {
        View layout = LayoutInflater.from(this)
                .inflate(R.layout.layout_lecturer_list, layoutContainer, false);
        layoutContainer.removeAllViews();
        layoutContainer.addView(layout);

        RecyclerView rvLecturers = layout.findViewById(R.id.rvLecturers);
        rvLecturers.setLayoutManager(new LinearLayoutManager(this));

        // Load lecturers from Firestore
        loadLecturers(rvLecturers);
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

                    if (lecturers.isEmpty()) {
                        Toast.makeText(this, R.string.no_lecturers_available, Toast.LENGTH_SHORT).show();
                    } else {
                        LecturerAdapter adapter = new LecturerAdapter(lecturers,
                                lecturer -> navigateToTimeSlots(lecturer.getName(),
                                        lecturer.getId()));
                        recyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, getString(R.string.error_loading_lecturers, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToTimeSlots(String locationId, String extraInfo) {
        // TODO: Replace this with TimeSlotActivity after it's created
        String message = getString(R.string.selected_location, locationId);
        if (extraInfo != null) {
            message += " (" + extraInfo + ")";
        }
        Toast.makeText(this, message + "\n\nTimeSlotActivity coming next!",
                Toast.LENGTH_LONG).show();

        /* UNCOMMENT THIS AFTER CREATING TimeSlotActivity:
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
        */
    }
}