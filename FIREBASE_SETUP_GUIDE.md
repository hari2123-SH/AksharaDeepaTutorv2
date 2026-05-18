# 🔥 Firebase Setup Guide — Akshara-Deepa v2.0

## STEP 1: Create Firebase Project
1. Go to https://console.firebase.google.com
2. Click "Add project" → Name it "AksharaDeepaTutor"
3. Disable Google Analytics (or enable — your choice) → Click "Create project"

## STEP 2: Add Android App to Firebase
1. In Firebase Console → click the Android icon (</> shaped)
2. Package name: com.aksharadeep.tutor
3. App nickname: Akshara-Deepa
4. Click "Register app"
5. Download google-services.json
6. Replace the placeholder file at: app/google-services.json

## STEP 3: Enable Firebase Authentication
1. Firebase Console → Build → Authentication → Get started
2. Sign-in method tab → Enable "Email/Password"
3. Click Save

## STEP 4: Enable Firestore Database
1. Firebase Console → Build → Firestore Database → Create database
2. Choose "Start in test mode" (for development)
3. Select a location close to India (asia-south1)
4. Click Done

## STEP 5: Enable Realtime Database
1. Firebase Console → Build → Realtime Database → Create database
2. Choose "Start in test mode"
3. Select location: asia-southeast1
4. Click Done

## STEP 6: Enable Firebase Analytics
1. Firebase Console → Analytics is auto-enabled for new projects
2. No extra steps needed

## STEP 7: Get Gemini AI API Key
1. Go to https://makersuite.google.com/app/apikey
2. Click "Create API Key"
3. Copy the key
4. Open: app/src/main/java/com/aksharadeep/tutor/utils/GeminiHelper.java
5. Replace: YOUR_GEMINI_API_KEY_HERE  with your actual key

## STEP 8: Build and Run
1. In Android Studio → File → Sync Project with Gradle Files
2. Wait for sync to complete
3. Click ▶ Run

## FEATURES ADDED IN v2.0
✅ Firebase Authentication — Real email/password login & register
✅ Firestore — Student profile + progress synced to cloud
✅ Realtime Database — Live leaderboard (updates instantly for all users)
✅ Firebase Analytics — Quiz completed, chapter completed, login events tracked
✅ Gemini AI Hints — AI-powered hints during quiz (needs API key)
✅ Streak Tracker — Daily study streak with Firebase sync
✅ Analytics Dashboard — Bar chart + Pie chart of study performance
✅ Leaderboard — Live ranking of all students by quiz average score

## FIRESTORE DATA STRUCTURE
users/
  {uid}/
    name: "Deepa K S"
    class: "Class 10, HKBK"
    email: "student@email.com"
    createdAt: timestamp
    progress/
      Science: { completedChapters, totalChapters, avgScore, lastUpdated }
      Mathematics: { ... }
      Social Studies: { ... }

## REALTIME DB STRUCTURE
leaderboard/
  {uid}/
    name: "Deepa K S"
    totalScore: 420
    quizCount: 6
    avgScore: 70
    updatedAt: timestamp
streaks/
  {uid}/
    currentStreak: 5
    lastStudyDate: timestamp
