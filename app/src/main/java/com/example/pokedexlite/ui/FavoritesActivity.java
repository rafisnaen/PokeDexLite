package com.example.pokedexlite.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
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
        binding.tvTitle.setText("FAVORITE POKEMON");

        dbHelper = new DatabaseHelper(this);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
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
}