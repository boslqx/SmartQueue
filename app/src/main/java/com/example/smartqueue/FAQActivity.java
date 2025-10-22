package com.example.smartqueue;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class FAQActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout faqContainer;

    // FAQ data
    private final String[][] faqs = {
            {"How do I book a facility?",
                    "Go to the Book tab, select your desired service (Discussion Room, Pool Table, etc.), choose a location, select date and time, then confirm your booking."},

            {"Can I cancel my booking?",
                    "Yes! Free bookings can be cancelled from My Queues. Paid bookings cannot be cancelled once confirmed."},

            {"How many hours can I book?",
                    "Most services allow 1-hour bookings. Music Room allows up to 3 consecutive hours."},

            {"How do I book a lecturer consultation?",
                    "Go to Book → Lecturer Consultation, select your lecturer, choose an available time slot (1 hour), and confirm."},

            {"What if a time slot shows as unavailable?",
                    "The slot is already booked by another user. Please choose a different time."},

            {"How far in advance can I book?",
                    "You can book up to 7 days in advance."},

            {"Are all services free?",
                    "Most services are free. Some premium services may require payment."},

            {"How do I view my bookings?",
                    "Go to My Queues tab to see all your active and past bookings."},

            {"Can I edit my profile?",
                    "Yes! Go to Settings → Edit Profile to update your information."},

            {"Who do I contact for support?",
                    "Go to Settings → Contact Support or email support@smartqueue.com"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faq_activity);

        btnBack = findViewById(R.id.btnBack);
        faqContainer = findViewById(R.id.faqContainer);

        btnBack.setOnClickListener(v -> finish());

        loadFAQs();
    }

    private void loadFAQs() {
        for (String[] faq : faqs) {
            View faqItem = createFAQItem(faq[0], faq[1]);
            faqContainer.addView(faqItem);
        }
    }

    private View createFAQItem(String question, String answer) {
        // Create CardView
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(12));
        cardView.setCardElevation(dpToPx(4));
        cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));

        // Create LinearLayout container
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        // Question TextView
        TextView tvQuestion = new TextView(this);
        tvQuestion.setText(question);
        tvQuestion.setTextSize(16);
        tvQuestion.setTextColor(getResources().getColor(R.color.text_dark));
        tvQuestion.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams questionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        questionParams.setMargins(0, 0, 0, dpToPx(8));
        tvQuestion.setLayoutParams(questionParams);

        // Answer TextView
        TextView tvAnswer = new TextView(this);
        tvAnswer.setText(answer);
        tvAnswer.setTextSize(14);
        tvAnswer.setTextColor(getResources().getColor(R.color.text_dark));
        tvAnswer.setVisibility(View.GONE); // Initially hidden

        // Add views to container
        container.addView(tvQuestion);
        container.addView(tvAnswer);
        cardView.addView(container);

        // Toggle answer visibility on click
        cardView.setOnClickListener(v -> {
            if (tvAnswer.getVisibility() == View.GONE) {
                tvAnswer.setVisibility(View.VISIBLE);
            } else {
                tvAnswer.setVisibility(View.GONE);
            }
        });

        return cardView;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}