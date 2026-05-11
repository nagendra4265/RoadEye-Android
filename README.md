# RoadEye — GitHub Ready Android Project

## Overview

RoadEye is a modern Android application built using Kotlin and Jetpack Compose for smart road damage reporting and complaint management.

The application supports:

* Citizen complaint reporting
* Officer complaint management
* Offline-first architecture
* Firebase backend integration
* Government-grade UI/UX
* AI-style road damage analysis
* Google Maps integration
* Push notifications

Designed for Andhra Pradesh and Telangana government demo presentations.

---

# Tech Stack

* Kotlin
* Jetpack Compose
* MVVM Clean Architecture
* Hilt Dependency Injection
* Firebase Authentication
* Firebase Firestore
* Firebase Storage
* Firebase Cloud Messaging
* Room Database
* Coroutines + Flow
* Material 3
* CameraX
* Google Maps SDK

---

# Project Structure

```text
RoadEye/
│
├── app/
│   ├── src/main/java/com/roadeye/
│   │   ├── ui/
│   │   │   ├── screens/
│   │   │   ├── components/
│   │   │   ├── theme/
│   │   │   └── navigation/
│   │   │
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── usecase/
│   │   │
│   │   ├── data/
│   │   │   ├── local/
│   │   │   ├── remote/
│   │   │   └── repository/
│   │   │
│   │   ├── di/
│   │   ├── utils/
│   │   └── service/
│   │
│   ├── res/
│   └── AndroidManifest.xml
│
├── build.gradle.kts
├── settings.gradle.kts
├── libs.versions.toml
└── README.md
```

---

# Features

## Citizen Module

* OTP login
* Report potholes
* CameraX image capture
* GPS auto location
* Severity selection
* Complaint tracking
* Notifications
* Complaint history

## Officer Module

* Officer login
* View all complaints
* Filter by status
* Map view
* Upload repair image
* Mark complaints resolved
* Complaint analytics dashboard

## Smart Features

* AI-style pothole detection
* Duplicate complaint detection
* Offline sync support
* Road health meter
* Telugu + English support
* Dark mode

---

# Firebase Collections

## complaints

```json
{
  "id": "",
  "userId": "",
  "title": "",
  "description": "",
  "imageUrl": "",
  "beforeImageUrl": "",
  "afterImageUrl": "",
  "latitude": 0.0,
  "longitude": 0.0,
  "address": "",
  "severity": "HIGH",
  "status": "SUBMITTED",
  "createdAt": 0,
  "updatedAt": 0
}
```

---

# Setup Instructions

## 1. Clone Repository

```bash
git clone https://github.com/your-username/RoadEye.git
```

## 2. Open in Android Studio

Use:

* Android Studio Narwhal or latest
* JDK 17
* Kotlin 2.0+

---

# Firebase Setup

## Enable:

* Authentication → Phone
* Firestore Database
* Firebase Storage
* Cloud Messaging

## Add Files

Place:

```text
google-services.json
```

inside:

```text
app/
```

---

# Required Permissions

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

---

# Recommended UI Design

## Citizen Dashboard

* Welcome banner
* Road health meter
* Nearby complaints
* Quick report button
* Complaint statistics

## Officer Dashboard

* Analytics cards
* Severity charts
* Complaint map
* Resolution tracking
* Pending alerts

---

# Recommended Animations

Use:

* AnimatedVisibility
* Lottie animations
* Fade transitions
* Expandable cards
* Skeleton loading

---

# GitHub README Preview

## RoadEye

AI-powered mobile governance platform for smart road maintenance.

### Features

* Smart road complaint reporting
* Real-time officer management
* Offline-first architecture
* Government-grade dashboard
* Firebase cloud sync
* Telugu support

### Built With

* Kotlin
* Jetpack Compose
* Firebase
* Room Database
* CameraX
* Google Maps

---

# GitHub Repository Suggestions

## Repository Name

```text
RoadEye-Android
```

## Topics

```text
android
kotlin
jetpack-compose
firebase
government-app
smart-city
road-maintenance
mvvm
clean-architecture
```

---

# Recommended Demo Flow

```text
Citizen Login
→ Capture Pothole
→ Upload Complaint
→ Officer Receives Complaint
→ Officer Resolves Complaint
→ Citizen Receives Notification
```

---

# APK Release Checklist

* Enable ProGuard
* Add release signing
* Firebase production config
* Add app icon
* Optimize images
* Enable crash reporting

---

# Future Enhancements

* ML pothole detection
* District analytics
* Voice complaint reporting
* QR-based road verification
* Smart city integration
* Officer attendance tracking
* Drone image support

---

# Suggested Screenshots for GitHub

* Splash Screen
* Role Selection
* Citizen Dashboard
* Complaint Capture
* Complaint Tracking
* Officer Dashboard
* Map View
* Notifications

---

# Final Positioning

RoadEye is designed as a mobile-first governance platform that improves:

* road maintenance transparency
* citizen participation
* officer accountability
* infrastructure monitoring

without requiring a web dashboard.
