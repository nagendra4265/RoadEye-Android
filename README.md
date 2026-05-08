# 🛣️ RoadEye — Smart Road Damage Reporter
### Government of Andhra Pradesh | R&B Department

> A production-ready Android application for reporting, tracking, and resolving road damage — empowering citizens and government officers in a single app.

---

## 📱 Screenshots Overview

| Splash | Role Selection | Citizen Dashboard | Officer Dashboard |
|--------|---------------|-------------------|-------------------|
| Government-grade splash with animation | Citizen / Officer role cards | Stats, health meter, complaint list | All complaints, priority alerts |

---

## 🏗️ Architecture

```
com.roadeye/
├── ui/
│   ├── screens/
│   │   ├── splash/          SplashScreen + ViewModel
│   │   ├── roleselection/   RoleSelectionScreen + ViewModel
│   │   ├── auth/            LoginScreen + OtpVerificationScreen + AuthViewModel
│   │   ├── citizen/         CitizenDashboardScreen + ViewModel
│   │   ├── officer/         OfficerDashboardScreen + OfficerComplaintDetailScreen
│   │   ├── complaint/       CaptureScreen + DetailScreen + TrackingScreen
│   │   ├── map/             MapScreen + ViewModel
│   │   ├── notifications/   NotificationsScreen + ViewModel
│   │   └── profile/         ProfileScreen + ViewModel
│   ├── components/          Reusable: ComplaintCard, StatCard, RoadHealthMeter, Badges
│   ├── theme/               Theme.kt, Typography.kt (Material 3)
│   └── navigation/          NavGraph.kt, Screen.kt
├── domain/
│   ├── model/               Complaint, User, Notification, enums
│   └── repository/          Repository interfaces
├── data/
│   ├── local/               Room DB, DAOs, Entities
│   └── repository/          Firebase + Room implementations
├── di/                      Hilt modules (DB, Firebase, Repos)
└── service/                 FCM Messaging Service
```

---

## ⚙️ Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Auth | Firebase Phone OTP + Email/Password |
| Database | Firebase Firestore (remote) + Room (offline cache) |
| Storage | Firebase Storage |
| Push Notifications | Firebase Cloud Messaging (FCM) |
| Location | Google Play Services Location (FusedLocationProvider) |
| Maps | Google Maps SDK + Maps Compose |
| Camera | CameraX |
| Image Loading | Coil |
| Reactive | Coroutines + StateFlow |
| Offline | Room + WorkManager sync |

---

## 🚀 Setup Guide

### 1. Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project: **RoadEye**
3. Add Android app → Package: `com.roadeye`
4. Download `google-services.json` → place in `app/`
5. Enable the following Firebase services:
   - ✅ **Authentication** → Phone (OTP) + Email/Password
   - ✅ **Firestore Database** → Start in test mode
   - ✅ **Storage** → Default bucket
   - ✅ **Cloud Messaging** (FCM)

### 2. Google Maps API

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Enable: **Maps SDK for Android**
3. Create an API Key
4. In `app/build.gradle.kts`, replace:
   ```kotlin
   manifestPlaceholders["MAPS_API_KEY"] = "YOUR_GOOGLE_MAPS_API_KEY"
   ```

### 3. Firestore Security Rules

```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /complaints/{doc} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update: if request.auth != null;
    }
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
    match /notifications/{doc} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 4. Officer Account Setup

Create officer accounts in Firebase Console:
- Authentication → Email/Password
- Add user: `officer@ap.gov.in` / `password123`
- In Firestore `users` collection, set `role: "OFFICER"`

### 5. Build & Run

```bash
# Clone
git clone https://github.com/nagendra4265/RoadEye-Android.git
cd RoadEye-Android

# Add your google-services.json to app/
# Update Maps API key in app/build.gradle.kts

# Build
./gradlew assembleDebug

# Install
./gradlew installDebug
```

---

## 📋 Firestore Schema

### `complaints` collection
```json
{
  "id": "auto-generated",
  "userId": "firebase_uid",
  "userName": "Ravi Kumar",
  "userPhone": "+919876543210",
  "title": "Large pothole on MG Road",
  "description": "Deep pothole near Benz Circle causing accidents",
  "imageUrl": "https://storage.firebase.../before.jpg",
  "beforeImageUrl": "https://storage.firebase.../before.jpg",
  "afterImageUrl": "https://storage.firebase.../after.jpg",
  "latitude": 16.5062,
  "longitude": 80.6480,
  "address": "MG Road, Vijayawada, AP",
  "district": "Krishna",
  "severity": "HIGH",
  "status": "IN_PROGRESS",
  "assignedOfficerId": "officer_uid",
  "assignedOfficerName": "Sri Venkat Rao",
  "officerNotes": "Repair scheduled for Monday",
  "roadHealthScore": 35,
  "resolvedAt": null,
  "createdAt": 1706000000000,
  "updatedAt": 1706001000000
}
```

### `users` collection
```json
{
  "id": "firebase_uid",
  "name": "Ravi Kumar",
  "phone": "+919876543210",
  "email": "",
  "role": "CITIZEN",
  "district": "Krishna",
  "ward": "Ward 12",
  "fcmToken": "fcm_device_token"
}
```

---

## 🎨 Color Palette

| Color | Hex | Usage |
|-------|-----|-------|
| Government Blue | `#1A3A6B` | Primary, headers |
| Dark Blue | `#0D2247` | Gradients |
| Saffron | `#FF8C00` | CTAs, accents (national color) |
| Success Green | `#2E7D32` | Resolved status |
| Danger Red | `#D32F2F` | High severity |
| Warning Orange | `#F57C00` | Medium/In-Progress |

---

## 🌟 Key Features

### Citizen Module
- 📱 Mobile OTP login (Firebase Phone Auth)
- 📸 CameraX photo capture of road damage
- 📍 Auto GPS location with address geocoding
- 🤖 AI-style pothole detection message after photo
- ⚠️ Duplicate complaint detection (nearby radius check)
- 📊 Road Health Meter (Green/Yellow/Red indicator)
- 📋 Complaint status tracking (Submitted → In Progress → Resolved)
- 🔔 FCM push notifications for updates
- 📶 Offline complaint saving (Room DB) + auto-sync

### Officer Module
- 🏛️ Email/Password login
- 📊 Dashboard with all complaints & stats
- 🔍 Filter by status (Pending / In Progress / Resolved)
- 🗺️ Interactive Google Maps view
- ✍️ Add officer notes & remarks
- 📸 Upload "after repair" photo
- ✅ Mark complaint as resolved

### Smart Features
- 🌙 Dark Mode support
- 🌐 Telugu + English bilingual UI
- 📱 Responsive Material 3 design
- 🏛️ Government-grade professional UI
- 📶 Offline-first architecture

---

## 🤝 Demo Data

For testing, the app includes:
- Sample complaint cards in the citizen dashboard
- Notification examples in the notifications screen
- Demo login bypass buttons (testing only — remove in production)

---

## 📌 Notes for Production

1. Remove demo login bypass buttons from LoginScreen.kt
2. Replace `google-services.json` placeholder with real file
3. Set up proper Firestore security rules
4. Configure Firebase App Check
5. Add real Maps API key with domain restrictions
6. Set up FCM notification topics per district
7. Implement proper officer role validation in Firestore rules

---

## 🏛️ Government Use Case

This app demonstrates how technology improves:
- **Citizen participation** in road maintenance
- **Accountability** through photo evidence & GPS
- **Transparency** with real-time status tracking
- **Efficiency** — single app for both citizens & officers
- **Data-driven** road health monitoring by district

---

*Built for Government of Andhra Pradesh | R&B Department*  
*రోడ్ ఐ — ప్రతి పౌరుడి అవకాశం*
