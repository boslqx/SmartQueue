SmartQueue - Campus Facility Booking System
A comprehensive Android application built with Java and Firebase that streamlines the booking process for campus facilities including discussion rooms, recreational equipment, music rooms, and lecturer consultations.
📱 Features
User Features

Multi-Service Booking System

Discussion Rooms (Large & Small)
Pool Tables
Ping Pong Tables
Music Room (up to 3 consecutive hours)
Lecturer Consultations


Smart Booking Management

Book up to 7 days in advance
Real-time availability checking
Consecutive time slot selection
Automatic expiry detection
Email confirmations for bookings


User Dashboard

Active bookings overview
Recent activity tracking
Booking statistics
Announcement carousel with auto-scroll


Profile Management

Edit personal information
Change password
View booking history
Custom notification preferences



Admin Features

Comprehensive Admin Dashboard

Announcement management
Lecturer management (CRUD operations)
Closed time slots management
Booking oversight and approval
User management (admin privileges)
Statistics and analytics


Lecturer Management

Add/Edit lecturer profiles
Manage consultation schedules
Track booked hours
Department organization


Booking Management

Approve/Reject bookings
Cancel bookings
Filter by status
View detailed booking information


User Administration

Grant/Remove admin privileges
Search and filter users
View user details
Track user activity



🛠️ Technical Stack

Language: Java
Platform: Android (Minimum SDK: API Level 24)
Backend: Firebase

Firebase Authentication (Email verification)
Cloud Firestore (Database)
Firebase Storage


Architecture: MVC Pattern
UI Components:

RecyclerView for dynamic lists
ViewPager2 for carousel
Material Design Components
Custom adapters and models



📋 Prerequisites

Android Studio (Arctic Fox or later)
JDK 8 or higher
Android SDK API Level 24+
Firebase account
Gmail account with App Password (for email notifications)



Installation Setup
1. Clone the repository
2. git clone https://github.com/yourusername/smartqueue.git
cd smartqueue
```

### 2. Firebase Configuration

1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com)
2. Add an Android app to your Firebase project
3. Download `google-services.json` and place it in `app/` directory
4. Enable Authentication (Email/Password)
5. Create Cloud Firestore database

### 3. Firestore Database Structure

Create the following collections:
```
firestore/
├── users/
│   └── {userId}/
│       ├── name: string
│       ├── email: string
│       ├── school: string
│       ├── role: string (admin/user)
│       ├── isAdmin: boolean
│       └── created_at: timestamp
│
├── services/
│   └── {serviceType}/
│       ├── name: string
│       ├── type: string
│       ├── available_from: string
│       ├── available_to: string
│       ├── max_duration: number
│       ├── is_paid: boolean
│       ├── price: number
│       └── layout_type: string
│
├── lecturers/
│   └── {lecturerId}/
│       ├── name: string
│       ├── email: string
│       ├── department: string
│       ├── office_location: string
│       ├── weekly_hours: number
│       ├── booked_hours: number
│       ├── consultation_schedule: map
│       └── created_at: timestamp
│
├── bookings/
│   └── {bookingId}/
│       ├── user_id: string
│       ├── user_email: string
│       ├── user_name: string
│       ├── service_type: string
│       ├── service_name: string
│       ├── location_id: string
│       ├── date: string (yyyy-MM-dd)
│       ├── start_time: string (HH:mm)
│       ├── end_time: string (HH:mm)
│       ├── duration: number
│       ├── status: string
│       ├── amount: number
│       ├── payment_status: string
│       └── created_at: timestamp
│
├── announcements/
│   └── {announcementId}/
│       ├── title: string
│       ├── message: string
│       ├── type: string (info/warning/success/event)
│       ├── priority: number
│       ├── active: boolean
│       └── created_at: timestamp
│
└── closed_slots/
    └── {slotId}/
        ├── date: string
        ├── start_time: string
        ├── end_time: string
        ├── service_type: string
        ├── reason: string
        └── created_at: timestamp
        4. Email Configuration
Update EmailSender.java with your Gmail credentials:
javaprivate static final String SENDER_EMAIL = "your-email@gmail.com";
private static final String SENDER_PASSWORD = "your-app-password"; // Gmail App Password
private static final String SENDER_NAME = "SmartQueue";
To get Gmail App Password:

Enable 2-Step Verification on your Google Account
Go to Google Account > Security > 2-Step Verification > App passwords
Generate a new app password
Use this password in the code

5. Student Email Domain
Configure allowed email domain in RegisterStep1Activity.java:
javaprivate static final String ALLOWED_EMAIL_DOMAIN = "@student.newiniti.edu.my";
6. Build Dependencies
Add to build.gradle (Module: app):
gradledependencies {
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'
    
    // Material Design
    implementation 'com.google.android.material:material:1.11.0'
    
    // RecyclerView
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    
    // CardView
    implementation 'androidx.cardview:cardview:1.0.0'
    
    // ViewPager2
    implementation 'androidx.viewpager2:viewpager2:1.0.0'
    
    // Email (JavaMail)
    implementation 'com.sun.mail:android-mail:1.6.7'
    implementation 'com.sun.mail:android-activation:1.6.7'
}
7. AndroidManifest.xml Permissions
Ensure these permissions are present:
xml<uses-permission android:name="android.permission.INTERNET" />
📱 App Structure
User Flow

Registration → Email verification → Dashboard
Login → Role-based redirect (Admin/User Dashboard)
Booking → Select service → Choose location → Pick time → Confirm → (Payment if required)
Management → View bookings → Cancel/Rebook

Admin Flow

Login → Admin Dashboard
Manage → Lecturers/Announcements/Bookings/Users
Monitor → Statistics and analytics

🎨 Key Features Implementation
Time Slot Management

Dynamic generation based on service availability
Real-time availability checking from Firestore
Consecutive slot validation for multi-hour bookings
Automatic expiry detection and status updates

Email Notifications

Automatic booking confirmations
HTML-formatted professional emails
Asynchronous sending (non-blocking)
Error handling and logging

Role-Based Access Control

Dual authentication system (users and admins)
Firestore-based role verification
Protected admin routes
Dynamic UI based on user role

Announcement System

Auto-scrolling carousel
Priority-based display
Type-based styling (info/warning/success/event)
Expiry management

🔐 Security Features

Email verification required for registration
Student email domain validation
Firebase Authentication
Secure password requirements (6+ characters)
Role-based access control
Protected admin endpoints

🐛 Troubleshooting
Common Issues
1. Email not sending

Verify Gmail App Password is correct
Check internet connection
Ensure JavaMail dependencies are added
Check logcat for email sending errors

2. Firebase connection issues

Verify google-services.json is in correct location
Check Firebase project configuration
Ensure internet permission is granted
Verify Firestore rules

3. Login redirects to wrong dashboard

Check user role in Firestore (role: "admin" or role: "user")
Verify isAdmin boolean field
Clear app data and re-login

4. Bookings not showing

Check Firestore collection structure
Verify field names match (snake_case)
Check date format (yyyy-MM-dd)
Ensure user_id matches authenticated user

📝 Usage Guide
For Users

Register Account

Use student email (@student.newiniti.edu.my)
Verify email before login
Complete profile setup


Make a Booking

Navigate to Book tab
Select service type
Choose location
Pick available date and time
Confirm booking
Receive email confirmation


Manage Bookings

View in My Queues
Cancel if needed (2+ hours advance)
Track booking status
Rebook expired slots



For Admins

Access Admin Panel

Login with admin credentials
Navigate to Admin Dashboard


Manage Lecturers

Add new lecturers
Set consultation schedules
Edit office locations
Track booking hours


Handle Bookings

Approve/reject pending bookings
View all bookings with filters
Cancel bookings if necessary


Post Announcements

Create new announcements
Set priority levels
Choose announcement type
Set expiry dates


User Management

Grant/remove admin privileges
Search and filter users
View user statistics



