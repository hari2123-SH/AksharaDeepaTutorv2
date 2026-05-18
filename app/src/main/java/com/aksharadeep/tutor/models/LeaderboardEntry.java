package com.aksharadeep.tutor.models;

public class LeaderboardEntry {
    public String userId;
    public String name;
    public String usn;
    public int totalScore;
    public int streak;
    public long updatedAt;
    public int rank;

    public LeaderboardEntry() {}

    public LeaderboardEntry(String userId, String name, String usn, int totalScore, int streak) {
        this.userId = userId;
        this.name = name;
        this.usn = usn;
        this.totalScore = totalScore;
        this.streak = streak;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getRankEmoji() {
        if (rank == 1) return "🥇";
        if (rank == 2) return "🥈";
        if (rank == 3) return "🥉";
        return "#" + rank;
    }
}
