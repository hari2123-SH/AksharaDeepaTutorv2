package com.aksharadeep.tutor.firebase;

import android.content.Context;
import android.os.Bundle;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {
    private static FirebaseManager instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final DatabaseReference realtimeDb;
    private FirebaseAnalytics analytics;

    private FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) instance = new FirebaseManager();
        return instance;
    }

    public FirebaseAuth getAuth() { return auth; }
    public FirebaseFirestore getFirestore() { return firestore; }

    public String getUid() {
        FirebaseUser u = auth.getCurrentUser();
        return u != null ? u.getUid() : null;
    }

    public void initAnalytics(Context context) {
        analytics = FirebaseAnalytics.getInstance(context);
    }

    // AUTH
    public void registerUser(String email, String password, String name, String usn, OnCompleteListener l) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(r -> {
                FirebaseUser u = r.getUser();
                if (u == null) { l.onFailure("Unknown error"); return; }
                Map<String,Object> p = new HashMap<>();
                p.put("name", name); p.put("usn", usn); p.put("email", email);
                p.put("totalScore", 0); p.put("streak", 0); p.put("chaptersCompleted", 0);
                p.put("quizzesTaken", 0); p.put("createdAt", System.currentTimeMillis());
                firestore.collection("users").document(u.getUid()).set(p)
                    .addOnSuccessListener(v -> l.onSuccess())
                    .addOnFailureListener(e -> l.onFailure(e.getMessage()));
            })
            .addOnFailureListener(e -> l.onFailure(e.getMessage()));
    }

    public void loginUser(String email, String password, OnCompleteListener l) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(r -> l.onSuccess())
            .addOnFailureListener(e -> l.onFailure(e.getMessage()));
    }

    public void logout() { auth.signOut(); }
    public FirebaseUser getCurrentUser() { return auth.getCurrentUser(); }
    public boolean isLoggedIn() { return auth.getCurrentUser() != null; }

    // FIRESTORE
    public void syncProgressToCloud(int done, int total, float avg, int quizzes) {
        FirebaseUser u = getCurrentUser(); if (u == null) return;
        Map<String,Object> d = new HashMap<>();
        d.put("chaptersCompleted", done); d.put("totalChapters", total);
        d.put("avgScore", avg); d.put("quizzesTaken", quizzes);
        d.put("lastSynced", System.currentTimeMillis());
        firestore.collection("users").document(u.getUid()).update(d);
    }

    public void getUserProfile(OnProfileLoadedListener l) {
        FirebaseUser u = getCurrentUser();
        if (u == null) { l.onFailure("Not logged in"); return; }
        firestore.collection("users").document(u.getUid()).get()
            .addOnSuccessListener(doc -> { if (doc.exists()) l.onLoaded(doc.getData()); else l.onFailure("Not found"); })
            .addOnFailureListener(e -> l.onFailure(e.getMessage()));
    }

    // LEADERBOARD
    public void updateLeaderboard(String name, String usn, int totalScore, int streak) {
        FirebaseUser u = getCurrentUser(); if (u == null) return;
        Map<String,Object> e = new HashMap<>();
        e.put("name", name); e.put("usn", usn); e.put("totalScore", totalScore);
        e.put("streak", streak); e.put("updatedAt", System.currentTimeMillis());
        realtimeDb.child("leaderboard").child(u.getUid()).setValue(e);
    }

    public DatabaseReference getLeaderboardRef() { return realtimeDb.child("leaderboard"); }

    // QUIZ RESULT
    public void saveQuizResultToCloud(String chapter, int score, int total, int subjectId) {
        FirebaseUser u = getCurrentUser(); if (u == null) return;
        Map<String,Object> r = new HashMap<>();
        r.put("chapterName", chapter); r.put("score", score); r.put("total", total);
        r.put("percentage", (score*100)/total); r.put("subjectId", subjectId);
        r.put("timestamp", System.currentTimeMillis());
        firestore.collection("users").document(u.getUid()).collection("quizResults").add(r);
    }

    // ANALYTICS
    public void logQuizCompleted(String chapter, int score) {
        if (analytics == null) return;
        Bundle b = new Bundle(); b.putString("chapter", chapter); b.putInt("score", score);
        analytics.logEvent("quiz_completed", b);
    }
    public void logChapterCompleted(String chapter, String subject) {
        if (analytics == null) return;
        Bundle b = new Bundle();
        b.putString("chapter", chapter);
        b.putString("subject", subject);
        analytics.logEvent("chapter_completed", b);
    }
    public void logScreenView(String screen) {
        if (analytics == null) return;
        Bundle b = new Bundle(); b.putString(FirebaseAnalytics.Param.SCREEN_NAME, screen);
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, b);
    }

    public void logLogin(String method) {
        if (analytics == null) return;
        Bundle b = new Bundle();
        b.putString(FirebaseAnalytics.Param.METHOD, method);
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, b);
    }

    public interface OnCompleteListener { void onSuccess(); void onFailure(String error); }
    public interface OnProfileLoadedListener { void onLoaded(Map<String,Object> data); void onFailure(String error); }
}
