package com.aksharadeep.tutor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StreakManager {
    private static final String PREFS = "StreakPrefs";
    private static final String KEY_LAST_DATE = "lastStudyDate";
    private static final String KEY_STREAK = "currentStreak";
    private static final String KEY_BEST_STREAK = "bestStreak";

    public static int getCurrentStreak(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_STREAK, 0);
    }

    public static int getBestStreak(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_BEST_STREAK, 0);
    }

    public static String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    // Call this whenever student studies (marks chapter or takes quiz)
    public static int recordStudyActivity(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String today = getTodayDate();
        String lastDate = prefs.getString(KEY_LAST_DATE, "");
        int streak = prefs.getInt(KEY_STREAK, 0);
        int best = prefs.getInt(KEY_BEST_STREAK, 0);

        if (today.equals(lastDate)) {
            return streak; // Already studied today
        }

        // Check if yesterday
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date todayDate = sdf.parse(today);
            Date lastStudyDate = lastDate.isEmpty() ? null : sdf.parse(lastDate);

            if (lastStudyDate != null) {
                long diff = todayDate.getTime() - lastStudyDate.getTime();
                long daysDiff = diff / (1000 * 60 * 60 * 24);
                if (daysDiff == 1) {
                    streak++; // Consecutive day
                } else {
                    streak = 1; // Streak broken
                }
            } else {
                streak = 1; // First time
            }
        } catch (Exception e) {
            streak = 1;
        }

        if (streak > best) best = streak;

        prefs.edit()
            .putString(KEY_LAST_DATE, today)
            .putInt(KEY_STREAK, streak)
            .putInt(KEY_BEST_STREAK, best)
            .apply();

        return streak;
    }

    public static String getStreakEmoji(int streak) {
        if (streak >= 30) return "🏆";
        if (streak >= 14) return "🔥🔥";
        if (streak >= 7)  return "🔥";
        if (streak >= 3)  return "⚡";
        if (streak >= 1)  return "✨";
        return "😴";
    }

    public static String getMotivationalMessage(int streak) {
        if (streak == 0) return "Start studying today to build your streak!";
        if (streak == 1) return "Great start! Come back tomorrow to build your streak!";
        if (streak < 7)  return "You're on a " + streak + "-day streak! Keep going!";
        if (streak < 14) return "Amazing! " + streak + " days strong! You're unstoppable!";
        if (streak < 30) return "Incredible " + streak + "-day streak! You're a study champion!";
        return "LEGENDARY! " + streak + " days! You are the TOP student!";
    }
}
