package com.example.pokedexlite.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pokedexlite.data.local.DatabaseHelper;
import com.example.pokedexlite.data.model.PokemonDetailResponse;
import com.example.pokedexlite.data.remote.PokeApiService;
import com.example.pokedexlite.data.remote.RetrofitClient;
import com.example.pokedexlite.databinding.ActivityDetailBinding;
import com.squareup.picasso.Picasso;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private DatabaseHelper dbHelper;
    private PokeApiService apiService;

    private int pokemonId;
    private String pokemonName;
    private String imageUrl;
    private String typeString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);
        apiService = RetrofitClient.getService();

        pokemonId = getIntent().getIntExtra("EXTRA_ID", -1);
        if (pokemonId == -1) {
            finish();
            return;
        }

        imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + pokemonId + ".png";
        Picasso.get().load(imageUrl).into(binding.imgDetail);
        loadDetail();

        updateFavoriteButtonState();

        binding.btnToggleFav.setOnClickListener(v -> {
            if (dbHelper.isFavorite(pokemonId)) {
                dbHelper.removeFavorite(pokemonId);
                Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.addFavorite(pokemonId, pokemonName, imageUrl, typeString);
                Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
            }
            updateFavoriteButtonState();
        });

        binding.btnAddToTeam.setOnClickListener(v -> {
            if (dbHelper.isInTeam(pokemonId)) {
                Toast.makeText(this, "Already in your team!", Toast.LENGTH_SHORT).show();
            } else {
                int slot = dbHelper.getAvailableTeamSlot();
                if (slot == -1) {
                    Toast.makeText(this, "Team is Full (Max 6)!", Toast.LENGTH_LONG).show();
                } else {
                    dbHelper.addToTeam(slot, pokemonId, pokemonName, imageUrl, typeString);
                    Toast.makeText(this, "Added to Team (Slot " + slot + ")", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadDetail() {
        apiService.getPokemonDetail(pokemonId).enqueue(new Callback<PokemonDetailResponse>() {
            @Override
            public void onResponse(Call<PokemonDetailResponse> call, Response<PokemonDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PokemonDetailResponse data = response.body();

                    pokemonName = data.getName();
                    binding.tvDetailName.setText(pokemonName);

                    // Parse Types
                    StringBuilder sb = new StringBuilder();
                    for (PokemonDetailResponse.TypeSlot slot : data.getTypes()) {
                        sb.append(slot.getType().getName()).append(", ");
                    }
                    if (sb.length() > 2) typeString = sb.substring(0, sb.length() - 2);
                    binding.tvDetailTypes.setText(typeString);

                    // Parse Stats
                    for (PokemonDetailResponse.StatSlot stat : data.getStats()) {
                        String statName = stat.getStat().getName();
                        int val = stat.getBaseStat();

                        if (statName.equals("hp")) binding.tvStatHp.setText("HP: " + val);
                        if (statName.equals("attack")) binding.tvStatAtk.setText("Attack: " + val);
                        if (statName.equals("defense")) binding.tvStatDef.setText("Defense: " + val);
                        if (statName.equals("speed")) binding.tvStatSpd.setText("Speed: " + val);
                    }
                }
            }

            @Override
            public void onFailure(Call<PokemonDetailResponse> call, Throwable t) {
                Toast.makeText(DetailActivity.this, "Failed to load details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFavoriteButtonState() {
        if (dbHelper.isFavorite(pokemonId)) {
            binding.btnToggleFav.setText("Remove Fav");
        } else {
            binding.btnToggleFav.setText("Add to Fav");
        }
    }
}