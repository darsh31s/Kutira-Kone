Markdown

# 🧵 Kutira-Kone — Zero-Waste Fabric Exchange

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![AI](https://img.shields.io/badge/AI-Gemini_API-4285F4?style=for-the-badge&logo=google&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-In_Development-orange?style=for-the-badge)

> **Kutira-Kone** means *"cut and connect"* — a hyper-local Android marketplace
> that turns textile waste into community wealth.

---

## 📖 Table of Contents

- [About the Project](#-about-the-project)
- [The Problem We Solve](#-the-problem-we-solve)
- [Key Features](#-key-features)
- [Screenshots](#-screenshots)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Database Schema](#-database-schema)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Firebase Setup](#firebase-setup)
  - [Gemini API Setup](#gemini-api-setup)
- [Project Structure](#-project-structure)
- [User Flows](#-user-flows)
- [Feature Matrix](#-feature-matrix)
- [Non-Functional Requirements](#-non-functional-requirements)
- [Impact Goals](#-impact-goals)
- [Success Criteria](#-success-criteria)
- [Contributing](#-contributing)
- [License](#-license)
- [Team](#-team)

---

## 🌿 About the Project

**Kutira-Kone** is a GenAI-powered Android application built for the
**Circular Economy**. It connects home-based tailors who have excess
fabric scraps with artisans, patchwork artists, and doll makers who
need small pieces of fabric — all within a **5 km hyper-local radius**.

By digitising the *Leftover Economy*, Kutira-Kone bridges the gap
between fabric surplus and fabric demand, driving sustainability,
cost reduction, and community collaboration in semi-urban and rural
India.
📦 Tailor has 0.5m of silk offcut → uploads in 60 seconds
🎨 Artisan needs silk scraps → finds it 2km away → requests swap
♻️ Trade completed → fabric stays out of landfill

text


---

## 🚨 The Problem We Solve

India generates approximately **4 million tonnes of textile waste**
per year. A large portion originates from home-based tailors and
small boutiques in semi-urban and rural communities.

| The Waste Side 🗑️ | The Demand Side 🎨 |
|---|---|
| Tailors accumulate fabric pieces (0.1–1m) after every order | Patchwork artists and doll makers need small fabric pieces |
| No practical way to sell or donate small scraps | Buying full meters is wasteful and expensive |
| Most scraps end up in landfills or are burned | No platform exists to discover local scrap availability |

**Gap:** There is no digital channel that connects village-level
fabric surplus with local demand — with search-by-radius, swap
capability, and AI-guided reuse ideas.

**Kutira-Kone fills that gap.**

---

## ✨ Key Features

### 🔐 Authentication
- Phone number + OTP login via Firebase Auth
- Role selection on first login: **Tailor / Artisan / Both**
- Persistent session with Firestore profile sync

### 📦 For Tailors (Vendors)
- Upload fabric scraps with photo (mandatory), material type,
  size, colour, condition, and price or swap offer
- Manage incoming buy and swap requests
- Accept, decline, or counter offers
- In-app chat after trade acceptance
- View ratings and reviews from buyers

### 🔍 For Artisans (Customers)
- Browse fabric scraps in a colorful Pinterest-style grid
- Switch to **Map View** with material-type pins
- Filter by material type, colour, and radius (1–20 km)
- Real-time updates — no manual refresh needed
- Offline browsing of cached listings

### 🤖 GenAI Design Ideas (Gemini API)
- Each listing shows **3 AI-generated DIY project suggestions**
  based on the fabric's material and size
- Dedicated **"Inspire Me"** screen — input any material and
  size to generate creative project ideas
- Suggestions include: project name, difficulty level,
  and a short description

### ⭐ Trust & Community
- Star rating system (1–5) after completed trades
- Text reviews stored and displayed on vendor profiles
- Average rating auto-calculated and synced in Firestore

### 🔔 Notifications
- Push notification on new buy/swap request
- Reminder 3 days before a listing expires
- In-app badge count for unread messages and requests

---

## 📱 Screenshots

> Screenshots will be added after the first build is complete.
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ Auth Screen │ │ Vendor Dashboard│ │Customer Discovery│
│ │ │ │ │ │
│ 📱 OTP Login │ │ + Upload FAB │ │ 🗺️ Map + Grid │
│ Role Selector │ │ My Listings │ │ Radius Filter │
│ │ │ Reviews │ │ AI Ideas │
└─────────────────┘ └─────────────────┘ └─────────────────┘

text


---

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose + Material 3 |
| **Min SDK** | API 26 (Android 8.0 Oreo) |
| **Realtime Database** | Firebase Firestore |
| **File Storage** | Firebase Storage |
| **Authentication** | Firebase Auth (Phone OTP) |
| **Push Notifications** | Firebase Cloud Messaging (FCM) |
| **Maps & Geolocation** | Google Maps SDK + Fused Location Provider |
| **AI / GenAI** | Google Gemini API |
| **Analytics** | Firebase Analytics + Crashlytics |
| **CI/CD** | GitHub Actions → Firebase App Distribution |

---

## 🏗️ Architecture

The project follows **MVVM (Model-View-ViewModel)** architecture
with a **Repository Pattern** for clean data access.
text

app/
├── ui/                     # Jetpack Compose screens & components
│   ├── auth/               # AuthScreen, RoleSelectionScreen
│   ├── vendor/             # VendorDashboard, UploadScreen
│   ├── customer/           # CustomerDashboard, ListingDetail
│   ├── chat/               # ChatScreen
│   ├── review/             # ReviewScreen
│   ├── inspire/            # InspireScreen (AI Ideas)
│   └── common/             # Shared UI components
│
├── viewmodel/              # ViewModels per feature
│   ├── AuthViewModel
│   ├── ListingViewModel
│   ├── ChatViewModel
│   ├── ReviewViewModel
│   └── AIViewModel
│
├── repository/             # Data layer (Firestore + Storage)
│   ├── UserRepository
│   ├── ListingRepository
│   ├── RequestRepository
│   ├── ChatRepository
│   └── AIRepository
│
├── model/                  # Data classes (User, Listing, etc.)
├── navigation/             # NavGraph, Routes
└── utils/                  # GeoHash, ImageCompressor, Constants
text


---

## 🗄️ Database Schema

### Firestore Collections

#### `users/{uid}`
```json
{
  "name": "Meena Sundaram",
  "phone": "+919876543210",
  "role": "tailor",
  "village": "Karaikudi",
  "location": "GeoPoint(10.0748, 78.7733)",
  "avg_rating": 4.6,
  "listingCount": 12,
  "profilePhotoURL": "https://storage.firebase..."
}
listings/{lid}
JSON

{
  "userId": "uid_abc123",
  "material": "Silk",
  "sizeMetres": 0.5,
  "colour": "Red",
  "condition": "new",
  "type": "sell",
  "photoURLs": ["https://storage.firebase..."],
  "price": 40,
  "swapOffer": null,
  "geoHash": "tdr1u",
  "location": "GeoPoint(10.0748, 78.7733)",
  "createdAt": "2025-01-15T10:30:00Z",
  "expiresAt": "2025-02-14T10:30:00Z",
  "status": "available"
}
requests/{rid}
JSON

{
  "listingId": "lid_xyz789",
  "requesterId": "uid_def456",
  "ownerId": "uid_abc123",
  "type": "swap",
  "swapOffer": "I have 0.3m cotton in blue",
  "status": "pending",
  "createdAt": "2025-01-15T11:00:00Z"
}
messages/{conversationId}/msgs
JSON

{
  "senderId": "uid_def456",
  "text": "Hi! Can we meet at the market tomorrow?",
  "timestamp": "2025-01-15T11:05:00Z",
  "isRead": false
}
reviews/{reviewId}
JSON

{
  "vendorId": "uid_abc123",
  "customerId": "uid_def456",
  "listingId": "lid_xyz789",
  "rating": 5,
  "comment": "Beautiful silk piece, exactly as described!",
  "createdAt": "2025-01-16T09:00:00Z"
}
designIdeas/{did}
JSON

{
  "listingId": "lid_xyz789",
  "ideas": [
    {
      "title": "Silk Coin Pouch",
      "difficulty": "Easy",
      "description": "Fold and stitch into a small 10x10cm pouch with a zip."
    },
    {
      "title": "Hair Scrunchie",
      "difficulty": "Easy",
      "description": "Wrap around elastic band for a luxurious silk scrunchie."
    },
    {
      "title": "Doll Dress",
      "difficulty": "Medium",
      "description": "Cut and sew a miniature dress for a 30cm doll figure."
    }
  ],
  "generatedAt": "2025-01-15T11:10:00Z"
}
🚀 Getting Started
Prerequisites
Before you begin, ensure you have the following installed:

Android Studio Hedgehog (2023.1.1) or later
JDK 17 or later
Android SDK API 26+
A Firebase project (free Spark plan works for development)
A Google Cloud project with Gemini API enabled
A Google Maps API key
Installation
1. Clone the repository

Bash

git clone https://github.com/your-username/kutira-kone.git
cd kutira-kone
2. Open in Android Studio

text

File → Open → select the kutira-kone folder
3. Create your local secrets file

Bash

# In the project root directory
touch local.properties
Add the following to local.properties:

properties

# Google Maps
MAPS_API_KEY=your_google_maps_api_key_here

# Google Gemini AI
GEMINI_API_KEY=your_gemini_api_key_here
⚠️ NEVER commit local.properties to version control.
It is already listed in .gitignore.

4. Sync Gradle

text

File → Sync Project with Gradle Files
Firebase Setup
Step 1 — Create a Firebase Project

Go to Firebase Console
Click "Add Project" → name it kutira-kone
Disable Google Analytics (optional for development)
Step 2 — Add Android App

In Firebase Console → Project Settings → Add App → Android
Package name: com.kutirakone.app
Download google-services.json
Place it in the app/ directory
text

kutira-kone/
└── app/
    └── google-services.json  ← place here
Step 3 — Enable Firebase Services

In Firebase Console, enable the following:

text

Authentication  → Phone (enable phone sign-in provider)
Firestore       → Create database in production mode
Storage         → Set up default bucket
Cloud Messaging → Already enabled by default
Step 4 — Deploy Firestore Security Rules

Copy and paste into Firestore → Rules:

JavaScript

rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Users: only owner can write
    match /users/{uid} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == uid;
    }

    // Listings: authenticated read, owner write
    match /listings/{lid} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth.uid == resource.data.userId;
    }

    // Requests: requester or owner can read/write
    match /requests/{rid} {
      allow read: if request.auth.uid == resource.data.requesterId
                  || request.auth.uid == resource.data.ownerId;
      allow create: if request.auth != null;
      allow update: if request.auth.uid == resource.data.ownerId;
    }

    // Messages: only participants can read/write
    match /messages/{cid}/msgs/{mid} {
      allow read, write: if request.auth != null;
    }

    // Reviews: authenticated read, customer write
    match /reviews/{rid} {
      allow read: if request.auth != null;
      allow create: if request.auth.uid == request.resource.data.customerId;
    }

    // Design Ideas: authenticated read, any write (AI-generated)
    match /designIdeas/{did} {
      allow read, write: if request.auth != null;
    }
  }
}
Step 5 — Deploy Storage Rules

Copy and paste into Storage → Rules:

JavaScript

rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /listings/{userId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId
                   && request.resource.size < 500 * 1024
                   && request.resource.contentType.matches('image/.*');
    }
    match /profiles/{userId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
  }
}
Gemini API Setup
Go to Google AI Studio
Click "Get API Key" → "Create API key"
Copy the key into local.properties as shown above
The app calls Gemini with the following prompt structure:
Kotlin

// Example prompt sent to Gemini API
val prompt = """
    I have a piece of ${material} fabric that is ${sizeMetres} metres.
    Suggest exactly 3 creative DIY craft projects I can make with it.
    For each project provide:
    - title (short name)
    - difficulty (Easy / Medium / Hard)
    - description (2 sentences max)
    Return as JSON array only.
""".trimIndent()
📁 Project Structure
kutira-kone/
│
├── app/
│ ├── src/main/
│ │ ├── java/com/kutirakone/app/
│ │ │ │
│ │ │ ├── MainActivity.kt
│ │ │ ├── KutiraKoneApp.kt
│ │ │ │
│ │ │ ├── navigation/
│ │ │ │ ├── NavGraph.kt
│ │ │ │ └── Routes.kt
│ │ │ │
│ │ │ ├── model/
│ │ │ │ ├── User.kt
│ │ │ │ ├── Listing.kt
│ │ │ │ ├── Request.kt
│ │ │ │ ├── Message.kt
│ │ │ │ ├── Review.kt
│ │ │ │ └── DesignIdea.kt
│ │ │ │
│ │ │ ├── repository/
│ │ │ │ ├── UserRepository.kt
│ │ │ │ ├── ListingRepository.kt
│ │ │ │ ├── RequestRepository.kt
│ │ │ │ ├── ChatRepository.kt
│ │ │ │ ├── ReviewRepository.kt
│ │ │ │ └── AIRepository.kt
│ │ │ │
│ │ │ ├── viewmodel/
│ │ │ │ ├── AuthViewModel.kt
│ │ │ │ ├── ListingViewModel.kt
│ │ │ │ ├── RequestViewModel.kt
│ │ │ │ ├── ChatViewModel.kt
│ │ │ │ ├── ReviewViewModel.kt
│ │ │ │ └── AIViewModel.kt
│ │ │ │
│ │ │ ├── ui/
│ │ │ │ ├── auth/
│ │ │ │ │ ├── AuthScreen.kt
│ │ │ │ │ └── RoleSelectionScreen.kt
│ │ │ │ │
│ │ │ │ ├── vendor/
│ │ │ │ │ ├── VendorDashboard.kt
│ │ │ │ │ ├── UploadScreen.kt
│ │ │ │ │ └── RequestManagementScreen.kt
│ │ │ │ │
│ │ │ │ ├── customer/
│ │ │ │ │ ├── CustomerDashboard.kt
│ │ │ │ │ ├── ListingDetailScreen.kt
│ │ │ │ │ └── MapViewScreen.kt
│ │ │ │ │
│ │ │ │ ├── chat/
│ │ │ │ │ └── ChatScreen.kt
│ │ │ │ │
│ │ │ │ ├── review/
│ │ │ │ │ └── ReviewScreen.kt
│ │ │ │ │
│ │ │ │ ├── inspire/
│ │ │ │ │ └── InspireScreen.kt
│ │ │ │ │
│ │ │ │ └── common/
│ │ │ │ ├── FabricCard.kt
│ │ │ │ ├── StarRating.kt
│ │ │ │ ├── AIIdeaCard.kt
│ │ │ │ ├── MaterialBadge.kt
│ │ │ │ └── LoadingSkeleton.kt
│ │ │ │
│ │ │ └── utils/
│ │ │ ├── GeoHashUtils.kt
│ │ │ ├── ImageCompressor.kt
│ │ │ ├── LocationUtils.kt
│ │ │ └── Constants.kt
│ │ │
│ │ └── res/
│ │ ├── values/
│ │ │ ├── colors.xml
│ │ │ ├── strings.xml ← English
│ │ │ └── themes.xml
│ │ ├── values-hi/
│ │ │ └── strings.xml ← Hindi
│ │ └── values-ta/
│ │ └── strings.xml ← Tamil
│ │
│ ├── google-services.json ← DO NOT COMMIT
│ └── build.gradle.kts
│
├── local.properties ← DO NOT COMMIT
├── build.gradle.kts
├── settings.gradle.kts
└── README.md

🗺️ User Flows
Tailor / Seller Flow
Download App
│
▼
Phone OTP Login
│
▼
Select Role: Tailor
│
▼
Set Village + Location
│
▼
Vendor Dashboard
│
├──► (+) Upload Scrap
│ │
│ ▼
│ Take Photo (mandatory)
│ Enter: Material / Size / Colour /
│ Condition / Price or Swap
│ Confirm GPS Location
│ Publish Listing
│
├──► Receive Push Notification (new request)
│ │
│ ▼
│ View Request → Accept / Decline / Counter
│ │
│ ▼
│ In-App Chat → Arrange Meetup
│ │
│ ▼
│ Mark as Complete
│ │
│ ▼
│ Rate the Buyer → Listing Auto-Archives
│
└──► View My Reviews & Rating

Artisan / Buyer Flow
Download App
│
▼
Phone OTP Login
│
▼
Select Role: Artisan
│
▼
Set Location
│
▼
Customer Dashboard
│
├──► Filter: Material + Radius + Colour
│
├──► Grid View / Map View
│ │
│ ▼
│ Tap Listing Card
│ │
│ ▼
│ View Photos / Size / Price / Distance
│ See 3 AI Design Ideas
│ │
│ ▼
│ Tap "Request Buy" or "Request Swap"
│ │
│ ▼
│ Chat with Seller → Meet → Exchange
│ │
│ ▼
│ Mark Complete → Leave Review
│
└──► "Inspire Me" → Enter material + size
│
▼
Get AI-generated project ideas

✅ Feature Matrix
Feature	Status	Priority
Photo-mandatory scrap upload	✅ Must Have	P0
Material-type filter (Silk/Cotton/Wool/Synthetic/Blend)	✅ Must Have	P0
Radius-based search (1–20 km)	✅ Must Have	P0
Map view with material-type colour pins	✅ Must Have	P0
Buy request button with FCM notification	✅ Must Have	P0
Swap request with counter-offer form	✅ Must Have	P0
In-app chat after trade acceptance	✅ Must Have	P0
User registration with OTP	✅ Must Have	P0
Listing auto-expiry (30 days)	✅ Must Have	P0
Grid catalog with thumbnail view	✅ Must Have	P0
GenAI design idea cards (3 per listing)	✅ Must Have	P0
Push notifications for requests	✅ Must Have	P0
Tamil / Hindi / English localisation	✅ Must Have	P0
Offline cached listing view	✅ Must Have	P1
AI image recognition for material auto-tagging	⭐ Good to Have	P2
Video upload for fabric texture	⭐ Good to Have	P2
Community forum / tips board	⭐ Good to Have	P2
Eco-badges for completed swaps	⭐ Good to Have	P2
Bulk listing (multiple scraps at once)	⭐ Good to Have	P2
QR code-based scrap label	⭐ Good to Have	P3
Seller analytics dashboard	⭐ Good to Have	P3
Augmented Reality patchwork try-on	⭐ Good to Have	P3
Carbon footprint tracker per trade	⭐ Good to Have	P3
Wishlist / save listing for later	⭐ Good to Have	P3
⚙️ Non-Functional Requirements
Category	Requirement
Performance	Cold start < 3 sec; feed loads < 2 sec on 4G
Availability	Firebase SLA ≥ 99.5%; offline caches last 50 listings
Scalability	Supports 50,000+ listings across 500+ villages
Usability	Min 4.5:1 contrast ratio; Tamil / Hindi / English
Security	HTTPS/TLS 1.2+; Firebase Security Rules enforced
Data Privacy	GPS fuzzed to 500m radius for public display
Storage	Images compressed to < 500KB; orphan images deleted after 60 days
Battery	Geofencing API used — not continuous GPS polling
Compliance	India DPDP Act 2023 — explicit consent for location & camera
Localisation	Currency: INR; Distance: km; Weight: grams/metres
🌱 Impact Goals
Goal	Target
Sustainability	Divert 500 kg of textile fabric from landfill in Year 1
Cost Reduction	Artisans source materials at 10–30% of market price
Community	Connect tailors and artisans within 5 km radius
Self-Employment	₹500–₹3,000/month supplementary income for tailor households
SDG Alignment	SDG 12 (Consumption), SDG 8 (Work), SDG 11 (Communities), SDG 10 (Inequalities)
Women Empowerment	Give women tailors and artisans a digital economic identity
🏆 Success Criteria
Mandatory Acceptance Criteria
 Upload screen blocks publish if no photo is attached
 Search screen has radius filter: 1 / 2 / 5 / 10 / 20 km
 Each material type has a distinct colour in the catalog grid
 Map view shows interactive pins with material type labels
 Each listing displays exactly 3 GenAI design idea cards
 App runs without crashes on a real device or emulator
KPI Targets (6 Months Post-Launch)
KPI	Target
Active Listings	> 200 across 3+ villages
Registered Users	> 150 (tailors + artisans)
Completed Trades	> 80 buy/swap transactions
AI Ideas Generated	> 500 GenAI idea views
App Store Rating	≥ 4.0 / 5.0 on Google Play
Avg. Listing Discovery	Relevant listing found within 2 km in < 60 seconds
Crash-Free Sessions	> 98% (Firebase Crashlytics)
🤝 Contributing
Contributions are welcome! Please follow these steps:

Fork the repository
Create your feature branch:
Bash

git checkout -b feature/your-feature-name
Commit your changes:
Bash

git commit -m "feat: add your feature description"
Push to the branch:
Bash

git push origin feature/your-feature-name
Open a Pull Request with a clear description
Commit Message Format
text

feat:     new feature
fix:      bug fix
docs:     documentation changes
style:    formatting, no logic change
refactor: code restructure
test:     adding tests
chore:    build process or tooling changes
📄 License
text

MIT License

Copyright (c) 2025 Kutira-Kone Project

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
👥 Team
Role	Name
Project Lead	—
Android Developer	—
UI/UX Designer	—
Firebase / Backend	—
AI Integration	—
🔗 Resources
Firebase Documentation
Jetpack Compose Guide
Google Maps SDK for Android
Google Gemini API
GeoHash Library for Android
Material 3 Design System