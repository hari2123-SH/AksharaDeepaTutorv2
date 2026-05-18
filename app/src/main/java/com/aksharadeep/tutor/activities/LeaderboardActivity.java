package com.aksharadeep.tutor.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.aksharadeep.tutor.R;
import com.aksharadeep.tutor.firebase.FirebaseManager;
import com.aksharadeep.tutor.models.LeaderboardEntry;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private List<LeaderboardEntry> entries = new ArrayList<>();
    private ProgressBar loading;
    private TextView tvEmpty;
    private FirebaseManager fb;
    private ValueEventListener leaderboardListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        fb = FirebaseManager.getInstance();
        fb.logScreenView("Leaderboard");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("🏆 Leaderboard");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.rv_leaderboard);
        loading      = findViewById(R.id.progress_leaderboard);
        tvEmpty      = findViewById(R.id.tv_leaderboard_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(entries);
        recyclerView.setAdapter(adapter);

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        loading.setVisibility(View.VISIBLE);
        leaderboardListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                entries.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    LeaderboardEntry e = new LeaderboardEntry();
                    e.userId     = child.getKey();
                    e.name       = getStr(child, "name");
                    e.usn        = getStr(child, "usn");
                    e.totalScore = getInt(child, "totalScore");
                    e.streak     = getInt(child, "streak");
                    entries.add(e);
                }
                // Sort by totalScore descending
                Collections.sort(entries, (a, b) -> b.totalScore - a.totalScore);
                for (int i = 0; i < entries.size(); i++) entries.get(i).rank = i + 1;

                loading.setVisibility(View.GONE);
                if (entries.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loading.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Could not load leaderboard.\nCheck your connection.");
            }
        };
        fb.getLeaderboardRef()
          .orderByChild("totalScore")
          .addValueEventListener(leaderboardListener);
    }

    private String getStr(DataSnapshot s, String key) {
        Object v = s.child(key).getValue(); return v != null ? v.toString() : "";
    }
    private int getInt(DataSnapshot s, String key) {
        Object v = s.child(key).getValue();
        if (v instanceof Long) return ((Long)v).intValue();
        if (v instanceof Integer) return (Integer)v;
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (leaderboardListener != null)
            fb.getLeaderboardRef().removeEventListener(leaderboardListener);
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }

    // ── ADAPTER ──────────────────────────────────────────
    static class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.VH> {
        private final List<LeaderboardEntry> data;
        LeaderboardAdapter(List<LeaderboardEntry> data) { this.data = data; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            LeaderboardEntry e = data.get(pos);
            h.tvRank.setText(e.getRankEmoji());
            h.tvName.setText(e.name);
            h.tvUsn.setText(e.usn);
            h.tvScore.setText(e.totalScore + " pts");
            h.tvStreak.setText("🔥 " + e.streak + " days");

            // Highlight top 3
            int bgColor;
            if (e.rank == 1)      bgColor = Color.parseColor("#FFF9C4");
            else if (e.rank == 2) bgColor = Color.parseColor("#F5F5F5");
            else if (e.rank == 3) bgColor = Color.parseColor("#FBE9E7");
            else                  bgColor = Color.WHITE;
            h.itemView.setBackgroundColor(bgColor);
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvRank, tvName, tvUsn, tvScore, tvStreak;
            VH(View v) {
                super(v);
                tvRank   = v.findViewById(R.id.tv_rank);
                tvName   = v.findViewById(R.id.tv_lb_name);
                tvUsn    = v.findViewById(R.id.tv_lb_usn);
                tvScore  = v.findViewById(R.id.tv_lb_score);
                tvStreak = v.findViewById(R.id.tv_lb_streak);
            }
        }
    }
}
