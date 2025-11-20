package com.example.smartqueue;

import android.os.AsyncTask;
import android.util.Log;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    private static final String TAG = "EmailSender";
    private static final String SENDER_EMAIL = "bosliangqx@gmail.com";
    private static final String SENDER_PASSWORD = "hlmh gnix pgjg fqeu"; // Use App Password!
    private static final String SENDER_NAME = "SmartQueue";

    // Send booking confirmation email
    public static void sendBookingConfirmation(BookingModel booking, String bookingId) {
        new SendEmailTask().execute(
                booking.getUserEmail(),
                "Booking Confirmation - SmartQueue",
                createBookingConfirmationHTML(booking, bookingId)
        );
    }

    // Create HTML email content for booking confirmation

    private static String createBookingConfirmationHTML(BookingModel booking, String bookingId) {
        String shortId = bookingId.substring(0, 8).toUpperCase();

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #BADFDB; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
                ".content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }" +
                ".booking-details { background-color: white; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
                ".detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #eee; }" +
                ".detail-label { font-weight: bold; color: #666; }" +
                ".detail-value { color: #333; }" +
                ".status-badge { background-color: #4CAF50; color: white; padding: 5px 15px; border-radius: 20px; display: inline-block; }" +
                ".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1 style='margin: 0; color: #333;'>🎉 Booking Confirmed!</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Hello " + (booking.getUserName() != null ? booking.getUserName() : "Valued Customer") + ",</p>" +
                "<p>Your booking has been successfully confirmed. Here are your booking details:</p>" +
                "<div class='booking-details'>" +
                "<div class='detail-row'>" +
                "<span class='detail-label'>Booking ID:</span>" +
                "<span class='detail-value'>" + shortId + "</span>" +
                "</div>" +
                "<div class='detail-row'>" +
                "<span class='detail-label'>Service:</span>" +
                "<span class='detail-value'>" + booking.getServiceName() + "</span>" +
                "</div>" +
                "<div class='detail-row'>" +
                "<span class='detail-label'>Location:</span>" +
                "<span class='detail-value'>" + booking.getLocationId() + "</span>" +
                "</div>" +
                "<div class='detail-row'>" +
                "<span class='detail-label'>Date:</span>" +
                "<span class='detail-value'>" + booking.getDate() + "</span>" +
                "</div>" +
                "<div class='detail-row'>" +
                "<span class='detail-label'>Time:</span>" +
                "<span class='detail-value'>" + booking.getTimeSlot() + "</span>" +
                "</div>" +
                "<div class='detail-row'>" +
                "<span class='detail-label'>Duration:</span>" +
                "<span class='detail-value'>" + booking.getDuration() + " hour" + (booking.getDuration() > 1 ? "s" : "") + "</span>" +
                "</div>" +
                "<div class='detail-row'>" +
                "<span class='detail-label'>Amount:</span>" +
                "<span class='detail-value'>" + booking.getFormattedAmount() + "</span>" +
                "</div>" +
                "<div class='detail-row'>" +
                "<span class='detail-label'>Status:</span>" +
                "<span class='status-badge'>CONFIRMED</span>" +
                "</div>" +
                "</div>" +
                "<p><strong>Important Notes:</strong></p>" +
                "<ul>" +
                "<li>Please arrive 5 minutes before your scheduled time</li>" +
                "<li>If you need to cancel, please do so at least 2 hours in advance</li>" +
                "<li>You can view your booking details anytime in the SmartQueue app</li>" +
                "</ul>" +
                "<p>Thank you for using SmartQueue!</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>This is an automated email. Please do not reply to this message.</p>" +
                "<p>© 2025 SmartQueue. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    // AsyncTask to send email in background thread
    private static class SendEmailTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String recipientEmail = params[0];
            String subject = params[1];
            String htmlContent = params[2];

            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject(subject);
                message.setContent(htmlContent, "text/html; charset=utf-8");

                Transport.send(message);

                Log.d(TAG, "Email sent successfully to: " + recipientEmail);
                return true;

            } catch (Exception e) {
                Log.e(TAG, "Failed to send email: " + e.getMessage(), e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Log.i(TAG, "Email delivery successful");
            } else {
                Log.e(TAG, "Email delivery failed");
            }
        }
    }
}