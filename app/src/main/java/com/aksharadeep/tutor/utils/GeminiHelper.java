package com.aksharadeep.tutor.utils;

import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiHelper {

    // Replace with your actual Gemini API key from Google AI Studio
    private static final String API_KEY = "YOUR_GEMINI_API_KEY_HERE";
    private static final String API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface HintCallback {
        void onHint(String hint);
        void onError(String error);
    }

    public static void getHintForQuestion(String questionText, String correctAnswer,
                                           String explanation, HintCallback callback) {
        executor.execute(() -> {
            try {
                String prompt = "You are a helpful tutor for Class 10 SSLC students in India. " +
                    "Give a SHORT, simple hint (max 2 sentences) to help understand this question. " +
                    "Do NOT give the answer directly.\n\n" +
                    "Question: " + questionText + "\n" +
                    "Correct Answer: " + correctAnswer + "\n" +
                    "Explanation context: " + explanation + "\n\n" +
                    "Give only the hint, nothing else:";

                String requestBody = "{\n" +
                    "  \"contents\": [{\"parts\": [{\"text\": \"" +
                    escapeJson(prompt) + "\"}]}],\n" +
                    "  \"generationConfig\": {\"maxOutputTokens\": 100, \"temperature\": 0.3}\n" +
                    "}";

                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);

                    String response = sb.toString();
                    // Parse text from JSON response
                    String hint = extractText(response);
                    mainHandler.post(() -> callback.onHint(hint));
                } else {
                    mainHandler.post(() -> callback.onError("API error: " + responseCode));
                }
                conn.disconnect();

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    public static void getStudyTip(String subjectName, String weakChapter, HintCallback callback) {
        executor.execute(() -> {
            try {
                String prompt = "Give a very short study tip (1-2 sentences) for a Class 10 SSLC student " +
                    "who is weak in " + subjectName + " specifically the chapter: " + weakChapter + ". " +
                    "Be encouraging and practical:";

                String requestBody = "{\n" +
                    "  \"contents\": [{\"parts\": [{\"text\": \"" +
                    escapeJson(prompt) + "\"}]}],\n" +
                    "  \"generationConfig\": {\"maxOutputTokens\": 80, \"temperature\": 0.4}\n" +
                    "}";

                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.getBytes("UTF-8"));
                }

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    String tip = extractText(sb.toString());
                    mainHandler.post(() -> callback.onHint(tip));
                } else {
                    mainHandler.post(() -> callback.onError("Could not get tip"));
                }
                conn.disconnect();
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private static String extractText(String json) {
        try {
            int start = json.indexOf("\"text\": \"") + 9;
            int end = json.indexOf("\"", start);
            if (start > 8 && end > start) {
                return json.substring(start, end)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");
            }
        } catch (Exception ignored) {}
        return "Think about the key concept in the question carefully!";
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
