package com.example.pokedexlite.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
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
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("MY DREAM TEAM");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.progressBar.setVisibility(View.GONE);
        dbHelper = new DatabaseHelper(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        hideSystemUI();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTeam();
    }

    private void loadTeam() {
        List<TeamItem> teamList = new ArrayList<>();
        Cursor cursor = dbHelper.getTeam();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    TeamItem item = new TeamItem();
                    item.dbId = cursor.getInt(0);
                    item.pokemonId = cursor.getInt(1);
                    item.name = cursor.getString(2);
                    item.imageUrl = cursor.getString(3);
                    item.types = cursor.getString(4);
                    item.note = cursor.getString(5);

                    teamList.add(item);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        if (teamList.isEmpty()) {
            Toast.makeText(this, "Your team is empty. Go add some Pokemon!", Toast.LENGTH_SHORT).show();
        }

        adapter = new TeamAdapter(teamList);
        binding.recyclerView.setAdapter(adapter);
    }
    static class TeamItem {
        int dbId;
        int pokemonId;
        String name;
        String imageUrl;
        String types;
        String note;
    }

    class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {
        List<TeamItem> list;

        public TeamAdapter(List<TeamItem> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team, parent, false);
            return new TeamViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
            TeamItem item = list.get(position);
            holder.tvSlot.setText("#" + (position + 1));

            holder.tvName.setText(item.name);
            if (item.types != null && !item.types.isEmpty()) {
                holder.tvTypes.setText(item.types);
            } else {
                holder.tvTypes.setText("-");
            }
            Picasso.get()
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.img);
            if (item.note == null || item.note.isEmpty()) {
                holder.tvNote.setText("Role: (Tap edit icon)");
            } else {
                holder.tvNote.setText("Role: " + item.note);
            }
            holder.btnEditNote.setOnClickListener(v -> {
                showEditNoteDialog(item);
            });
            holder.btnRemove.setOnClickListener(v -> {
                new AlertDialog.Builder(TeamActivity.this)
                        .setTitle("Remove Pokemon")
                        .setMessage("Remove " + item.name + " from team?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            dbHelper.removeFromTeam(item.dbId);
                            Toast.makeText(TeamActivity.this, "Removed " + item.name, Toast.LENGTH_SHORT).show();
                            loadTeam();
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(TeamActivity.this, DetailActivity.class);
                intent.putExtra("EXTRA_NAME", item.name);
                intent.putExtra("EXTRA_ID", item.pokemonId);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class TeamViewHolder extends RecyclerView.ViewHolder {
            TextView tvSlot, tvName, tvTypes, tvNote;
            ImageView img;
            ImageButton btnRemove, btnEditNote;

            public TeamViewHolder(View itemView) {
                super(itemView);
                tvSlot = itemView.findViewById(R.id.tvSlot);
                tvName = itemView.findViewById(R.id.tvTeamName);
                tvTypes = itemView.findViewById(R.id.tvTeamTypes);
                tvNote = itemView.findViewById(R.id.tvTeamNote);
                img = itemView.findViewById(R.id.imgTeam);
                btnRemove = itemView.findViewById(R.id.btnRemove);
                btnEditNote = itemView.findViewById(R.id.btnEditNote);
            }
        }
    }

    private void showEditNoteDialog(TeamItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Role for " + item.name);
        builder.setMessage("E.g., Tank, Sweeper, Support");

        final EditText input = new EditText(this);
        input.setText(item.note);
        input.setSingleLine(true);
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new  android.widget.FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 50;
        params.rightMargin = 50;
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newNote = input.getText().toString().trim();
            dbHelper.updateTeamNote(item.dbId, newNote);
            loadTeam();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
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