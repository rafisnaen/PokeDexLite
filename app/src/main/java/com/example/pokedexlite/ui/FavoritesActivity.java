package com.example.pokedexlite.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.pokedexlite.data.local.DatabaseHelper;
import com.example.pokedexlite.data.model.PokemonListResponse;
import com.example.pokedexlite.databinding.ActivityMainBinding;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("My Favorites");
        binding.progressBar.setVisibility(View.GONE);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("FAVORITE POKEMON");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.progressBar.setVisibility(View.GONE);

        dbHelper = new DatabaseHelper(this);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        hideSystemUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    private void loadFavorites() {
        Cursor cursor = dbHelper.getAllFavorites();
        List<PokemonListResponse.PokemonResult> list = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String url = cursor.getString(2);
                PokemonListResponse.PokemonResult pokemon = new PokemonListResponse.PokemonResult();
                pokemon.setManualData(name, url);

                list.add(pokemon);
            } while (cursor.moveToNext());
            cursor.close();
        }

        PokemonAdapter adapter = new PokemonAdapter(list);
        binding.recyclerView.setAdapter(adapter);
    }
    private void hideSystemUI() {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars());
    }
}