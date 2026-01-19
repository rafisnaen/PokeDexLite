package com.example.pokedexlite.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.pokedexlite.R;
import com.example.pokedexlite.data.local.DatabaseHelper;
import com.example.pokedexlite.data.model.EvolutionChainResponse;
import com.example.pokedexlite.data.model.PokemonDetailResponse;
import com.example.pokedexlite.data.model.PokemonSpeciesResponse;
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
    private TextView tvEvolutionError;
    private HorizontalScrollView scrollEvolution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tvEvolutionError = findViewById(R.id.tvEvolutionError);
        scrollEvolution = findViewById(R.id.scrollEvolution);

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
        loadSpecies();

        setupButtons();
        hideSystemUI();
    }

    private void loadDetail() {
        apiService.getPokemonDetail(String.valueOf(pokemonId)).enqueue(new Callback<PokemonDetailResponse>() {
            @Override
            public void onResponse(Call<PokemonDetailResponse> call, Response<PokemonDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PokemonDetailResponse data = response.body();

                    pokemonName = data.getName();
                    binding.tvDetailName.setText(pokemonName);

                    StringBuilder sbType = new StringBuilder();
                    for (PokemonDetailResponse.TypeSlot slot : data.getTypes()) {
                        sbType.append(slot.getType().getName()).append(", ");
                    }
                    if (sbType.length() > 2) typeString = sbType.substring(0, sbType.length() - 2);
                    binding.tvDetailTypes.setText(typeString);

                    StringBuilder sbAbility = new StringBuilder();
                    if (data.getAbilities() != null) {
                        for (PokemonDetailResponse.AbilitySlot slot : data.getAbilities()) {
                            sbAbility.append(slot.getAbility().getName()).append("\n");
                        }
                    } else {
                        binding.tvAbilities.setText("No abilities found.");
                    }
                    binding.tvAbilities.setText(sbAbility.toString().trim());

                    for (PokemonDetailResponse.StatSlot stat : data.getStats()) {
                        String name = stat.getStat().getName();
                        int val = stat.getBaseStat();
                        if (name.equals("hp")) binding.tvStatHp.setText("HP: " + val);
                        else if (name.equals("attack")) binding.tvStatAtk.setText("Attack: " + val);
                        else if (name.equals("defense")) binding.tvStatDef.setText("Defense: " + val);
                        else if (name.equals("speed")) binding.tvStatSpd.setText("Speed: " + val);
                    }
                } else {
                    // Handle jika request detail sukses tapi body kosong
                    binding.tvAbilities.setText("Failed to load details.");
                }
            }
            @Override
            public void onFailure(Call<PokemonDetailResponse> call, Throwable t) {
                Toast.makeText(DetailActivity.this, "Err Detail: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                binding.tvAbilities.setText("Failed to load details.");
            }
        });
    }

    private void loadSpecies() {
        apiService.getPokemonSpecies(pokemonId).enqueue(new Callback<PokemonSpeciesResponse>() {
            @Override
            public void onResponse(Call<PokemonSpeciesResponse> call, Response<PokemonSpeciesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PokemonSpeciesResponse species = response.body();
                    binding.tvDescription.setText(species.getDescription());
                    String evoUrl = species.getEvolutionChainUrl();
                    if (evoUrl != null && !evoUrl.isEmpty()) {
                        loadEvolution(evoUrl);
                    } else {
                        showEvolutionError();
                    }
                } else {
                    binding.tvDescription.setText("No description provided for this pokemon");
                    showEvolutionError();
                }
            }

            @Override
            public void onFailure(Call<PokemonSpeciesResponse> call, Throwable t) {
                binding.tvDescription.setText("No description provided for this pokemon");
                showEvolutionError();
            }
        });
    }

    private void loadEvolution(String url) {
        apiService.getEvolutionChain(url).enqueue(new Callback<EvolutionChainResponse>() {
            @Override
            public void onResponse(Call<EvolutionChainResponse> call, Response<EvolutionChainResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EvolutionChainResponse.ChainLink chain = response.body().getChain();
                    if (chain != null) {
                        if(tvEvolutionError != null) tvEvolutionError.setVisibility(View.GONE);
                        if(scrollEvolution != null) scrollEvolution.setVisibility(View.VISIBLE);

                        // Render
                        binding.layoutEvolution.removeAllViews();
                        renderEvolutionChain(chain);
                    } else {
                        showEvolutionError();
                    }
                } else {
                    showEvolutionError();
                }
            }

            @Override
            public void onFailure(Call<EvolutionChainResponse> call, Throwable t) {
                showEvolutionError();
            }
        });
    }
    private void showEvolutionError() {
        if (tvEvolutionError != null) {
            tvEvolutionError.setVisibility(View.VISIBLE);
            tvEvolutionError.setText("No evolution chain provided");
        }
        if (scrollEvolution != null) {
            scrollEvolution.setVisibility(View.GONE);
        }
    }

    private void renderEvolutionChain(EvolutionChainResponse.ChainLink currentChain) {
        if (currentChain == null) return;
        View view = LayoutInflater.from(this).inflate(R.layout.item_pokemon_small, binding.layoutEvolution, false);

        TextView tvName = view.findViewById(R.id.tvNameSmall);
        ImageView img = view.findViewById(R.id.imgSmall);

        tvName.setText(currentChain.getSpeciesName());

        int id = currentChain.getSpeciesId();
        String url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + id + ".png";

        Picasso.get().load(url).resize(150, 150).centerInside().into(img);

        binding.layoutEvolution.addView(view);
        if (currentChain.getEvolvesTo() != null && !currentChain.getEvolvesTo().isEmpty()) {
            ImageView arrow = new ImageView(this);
            arrow.setImageResource(android.R.drawable.ic_media_play);
            arrow.setPadding(8,0,8,0);
            binding.layoutEvolution.addView(arrow);
            renderEvolutionChain(currentChain.getEvolvesTo().get(0));
        }
    }

    private void setupButtons() {
        updateFavoriteButtonState();

        binding.btnToggleFav.setOnClickListener(v -> {
            if (dbHelper.isFavorite(pokemonId)) {
                dbHelper.removeFavorite(pokemonId);
                Toast.makeText(this, "Removed Fav", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.addFavorite(pokemonId, pokemonName, imageUrl, typeString);
                Toast.makeText(this, "Added Fav", Toast.LENGTH_SHORT).show();
            }
            updateFavoriteButtonState();
        });

        binding.btnAddToTeam.setOnClickListener(v -> {
            if (dbHelper.isInTeam(pokemonId)) {
                Toast.makeText(this, "Already in Team!", Toast.LENGTH_SHORT).show();
            } else {
                int slot = dbHelper.getAvailableTeamSlot();
                if (slot == -1) Toast.makeText(this, "Team Full!", Toast.LENGTH_SHORT).show();
                else {
                    dbHelper.addToTeam(slot, pokemonId, pokemonName, imageUrl, typeString, "");
                    Toast.makeText(this, "Added to Slot " + slot, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateFavoriteButtonState() {
        binding.btnToggleFav.setText(dbHelper.isFavorite(pokemonId) ? "Remove Fav" : "Add to Fav");
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