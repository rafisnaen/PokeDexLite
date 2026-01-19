package com.example.pokedexlite.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokedexlite.R;
import com.example.pokedexlite.data.local.DatabaseHelper;
import com.example.pokedexlite.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Search History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.progressBar.setVisibility(View.GONE);

        loadHistory();
        hideSystemUI();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadHistory() {
        List<HistoryItem> historyList = new ArrayList<>();
        Cursor cursor = dbHelper.getHistory();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    HistoryItem item = new HistoryItem();
                    item.pokemonId = cursor.getInt(2);
                    item.name = cursor.getString(3);
                    item.timestamp = cursor.getLong(4);
                    historyList.add(item);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        HistoryAdapter adapter = new HistoryAdapter(historyList);
        binding.recyclerView.setAdapter(adapter);
    }

    static class HistoryItem {
        int pokemonId;
        String name;
        long timestamp;
    }

    class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryVH> {
        List<HistoryItem> list;
        public HistoryAdapter(List<HistoryItem> list) { this.list = list; }

        @NonNull @Override
        public HistoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
            return new HistoryVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryVH holder, int position) {
            HistoryItem item = list.get(position);
            holder.tvName.setText(item.name.substring(0, 1).toUpperCase() + item.name.substring(1));
            holder.tvQuery.setText("#" + item.pokemonId);

            holder.itemView.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(HistoryActivity.this, DetailActivity.class);
                intent.putExtra("EXTRA_NAME", item.name);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class HistoryVH extends RecyclerView.ViewHolder {
            TextView tvName, tvQuery;
            public HistoryVH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvHistoryName);
                tvQuery = v.findViewById(R.id.tvHistoryQuery);
            }
        }
    }

    private void hideSystemUI() {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
            windowInsetsController.hide(WindowInsetsCompat.Type.statusBars());
        }
    }
}