package com.example.pokedexlite.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pokedexlite.R;
import com.example.pokedexlite.data.local.DatabaseHelper;
import com.example.pokedexlite.databinding.ActivityMainBinding;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class TeamActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DatabaseHelper dbHelper;
    private TeamAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTitle("My Dream Team (6 Slots)");
        binding.progressBar.setVisibility(View.GONE);
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("MY DREAM TEAM");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.progressBar.setVisibility(View.GONE);

        dbHelper = new DatabaseHelper(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTeam();
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    private void loadTeam() {
        List<TeamItem> teamList = new ArrayList<>();
        Cursor cursor = dbHelper.getTeam();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                TeamItem item = new TeamItem();
                item.slot = cursor.getInt(0);
                item.id = cursor.getInt(1);
                item.name = cursor.getString(2);
                item.imageUrl = cursor.getString(3);
                item.types = cursor.getString(4);
                teamList.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (teamList.isEmpty()) {
            Toast.makeText(this, "Team is empty!", Toast.LENGTH_SHORT).show();
        }

        adapter = new TeamAdapter(teamList);
        binding.recyclerView.setAdapter(adapter);
    }

    static class TeamItem {
        int slot;
        int id;
        String name;
        String imageUrl;
        String types;
    }

    class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {
        List<TeamItem> list;

        public TeamAdapter(List<TeamItem> list) { this.list = list; }

        @NonNull
        @Override
        public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team, parent, false);
            return new TeamViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
            TeamItem item = list.get(position);
            holder.tvSlot.setText("#" + item.slot);
            holder.tvName.setText(item.name);
            holder.tvTypes.setText(item.types);
            Picasso.get().load(item.imageUrl).into(holder.img);
            holder.btnRemove.setOnClickListener(v -> {
                dbHelper.getWritableDatabase().delete("team", "slot=?", new String[]{String.valueOf(item.slot)});
                Toast.makeText(TeamActivity.this, "Removed from Slot " + item.slot, Toast.LENGTH_SHORT).show();
                loadTeam();
            });

            holder.itemView.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(TeamActivity.this, DetailActivity.class);
                intent.putExtra("EXTRA_ID", item.id);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class TeamViewHolder extends RecyclerView.ViewHolder {
            TextView tvSlot, tvName, tvTypes;
            ImageView img;
            ImageButton btnRemove;

            public TeamViewHolder(View itemView) {
                super(itemView);
                tvSlot = itemView.findViewById(R.id.tvSlot);
                tvName = itemView.findViewById(R.id.tvTeamName);
                tvTypes = itemView.findViewById(R.id.tvTeamTypes);
                img = itemView.findViewById(R.id.imgTeam);
                btnRemove = itemView.findViewById(R.id.btnRemove);
            }
        }
    }
}