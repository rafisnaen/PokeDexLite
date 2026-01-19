package com.example.pokedexlite.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pokedexlite.R;
import com.example.pokedexlite.data.local.DatabaseHelper;
import com.example.pokedexlite.data.model.EvolutionChainResponse;
import com.example.pokedexlite.data.model.PokemonDetailResponse;
import com.example.pokedexlite.data.model.PokemonSpeciesResponse;
import com.example.pokedexlite.data.remote.PokeApiService;
import com.example.pokedexlite.data.remote.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private TextView tvName, tvWeight, tvHeight, tvDesc, tvHpVal, tvAtkVal, tvDefVal;
    private ImageView ivImage;
    private ProgressBar progressHp, progressAtk, progressDef;
    private LinearLayout layoutTypes, layoutEvolution;
    private ImageButton btnBack;
    private MaterialButton btnFavorite, btnAddTeam;

    private DatabaseHelper dbHelper;
    private PokeApiService apiService;
    private int currentPokemonId = -1;
    private String currentPokemonName = "";
    private String currentImageUrl = "";
    private String currentTypes = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        dbHelper = new DatabaseHelper(this);
        apiService = RetrofitClient.getClient().create(PokeApiService.class);

        initViews();
        String pokemonName = getIntent().getStringExtra("EXTRA_NAME");
        int pokemonId = getIntent().getIntExtra("EXTRA_ID", -1);

        if (pokemonName != null) {
            loadPokemonDetail(pokemonName);
        } else if (pokemonId != -1) {
            loadPokemonDetail(String.valueOf(pokemonId));
        }
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_detail_name);
        tvWeight = findViewById(R.id.tv_weight);
        tvHeight = findViewById(R.id.tv_height);
        tvDesc = findViewById(R.id.tv_description);
        ivImage = findViewById(R.id.iv_detail_image);
        progressHp = findViewById(R.id.progress_hp);
        progressAtk = findViewById(R.id.progress_atk);
        progressDef = findViewById(R.id.progress_def);

        tvHpVal = findViewById(R.id.tv_stat_hp_val);
        tvAtkVal = findViewById(R.id.tv_stat_atk_val);
        tvDefVal = findViewById(R.id.tv_stat_def_val);

        layoutTypes = findViewById(R.id.layout_types);
        layoutEvolution = findViewById(R.id.layout_evolution);
        btnBack = findViewById(R.id.btn_back);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnAddTeam = findViewById(R.id.btn_add_team);

        btnBack.setOnClickListener(v -> finish());
        btnFavorite.setOnClickListener(v -> {
            if (currentPokemonId == -1) return;
            if (dbHelper.isFavorite(currentPokemonId)) {
                dbHelper.removeFavorite(currentPokemonId);
                updateFavoriteButtonState(false);
                Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.addFavorite(currentPokemonId, currentPokemonName, currentImageUrl);
                updateFavoriteButtonState(true);
                Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
            }
        });
        btnAddTeam.setOnClickListener(v -> {
            if (currentPokemonId == -1) return;
            if (dbHelper.getTeamCount() >= 6) {
                Toast.makeText(this, "Team is Full (Max 6)!", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.addToTeam(currentPokemonId, currentPokemonName, currentImageUrl, currentTypes);
                Toast.makeText(this, "Added to Team!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFavoriteButtonState(boolean isFav) {
        if (isFav) {
            btnFavorite.setText("Favorited");
            btnFavorite.setIconResource(R.drawable.ic_launcher_background);
        } else {
            btnFavorite.setText("Favorite");
            btnFavorite.setIconResource(R.drawable.ic_launcher_foreground);
        }
    }

    private void loadPokemonDetail(String nameOrId) {
        apiService.getPokemonDetail(nameOrId).enqueue(new Callback<PokemonDetailResponse>() {
            @Override
            public void onResponse(Call<PokemonDetailResponse> call, Response<PokemonDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PokemonDetailResponse data = response.body();
                    updateBasicUI(data);
                    dbHelper.addHistory(data.getName(), data.getId(), data.getName());
                    loadPokemonSpecies(data.getId());
                }
            }
            @Override
            public void onFailure(Call<PokemonDetailResponse> call, Throwable t) {
                Toast.makeText(DetailActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBasicUI(PokemonDetailResponse data) {
        currentPokemonId = data.getId();
        currentPokemonName = data.getName();
        currentImageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + data.getId() + ".png";

        tvName.setText(data.getName());
        Picasso.get().load(currentImageUrl).into(ivImage);

        tvWeight.setText((data.getWeight() / 10.0) + " KG");
        tvHeight.setText((data.getHeight() / 10.0) + " M");

        if (data.getStats().size() >= 3) {
            int hp = data.getStats().get(0).getBaseStat();
            int atk = data.getStats().get(1).getBaseStat();
            int def = data.getStats().get(2).getBaseStat();

            progressHp.setProgress(hp);
            tvHpVal.setText(String.valueOf(hp));

            progressAtk.setProgress(atk);
            tvAtkVal.setText(String.valueOf(atk));

            progressDef.setProgress(def);
            tvDefVal.setText(String.valueOf(def));
        }

        layoutTypes.removeAllViews();
        StringBuilder typesBuilder = new StringBuilder();

        for (PokemonDetailResponse.TypeSlot slot : data.getTypes()) {
            String typeName = slot.getType().getName();

            if (typesBuilder.length() > 0) typesBuilder.append(" / ");
            typesBuilder.append(typeName.substring(0, 1).toUpperCase() + typeName.substring(1));

            TextView chip = new TextView(this);
            chip.setText(typeName.toUpperCase());
            chip.setTextColor(Color.WHITE);
            chip.setTextSize(12);
            chip.setPadding(30, 10, 30, 10);
            chip.setBackgroundResource(R.drawable.bg_type_pill);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            chip.setLayoutParams(params);
            layoutTypes.addView(chip);
        }
        currentTypes = typesBuilder.toString();
        updateFavoriteButtonState(dbHelper.isFavorite(currentPokemonId));
    }
    private void loadPokemonSpecies(int id) {
        apiService.getPokemonSpecies(id).enqueue(new Callback<PokemonSpeciesResponse>() {
            @Override
            public void onResponse(Call<PokemonSpeciesResponse> call, Response<PokemonSpeciesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PokemonSpeciesResponse species = response.body();
                    String flavorText = "No description provided for this pokemon";
                    if (species.getFlavorTextEntries() != null) {
                        for (PokemonSpeciesResponse.FlavorTextEntry entry : species.getFlavorTextEntries()) {
                            if (entry.getLanguage().getName().equals("en")) {
                                flavorText = entry.getFlavorText().replace("\n", " ");
                                break;
                            }
                        }
                    }
                    tvDesc.setText(flavorText);
                    if (species.getEvolutionChain() != null) {
                        loadEvolutionChain(species.getEvolutionChain().getUrl());
                    } else {
                        showNoEvolution();
                    }
                }
            }
            @Override
            public void onFailure(Call<PokemonSpeciesResponse> call, Throwable t) {
                tvDesc.setText("No description provided for this pokemon");
                showNoEvolution();
            }
        });
    }
    private void loadEvolutionChain(String url) {
        apiService.getEvolutionChain(url).enqueue(new Callback<EvolutionChainResponse>() {
            @Override
            public void onResponse(Call<EvolutionChainResponse> call, Response<EvolutionChainResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    renderEvolutionChain(response.body());
                } else {
                    showNoEvolution();
                }
            }
            @Override
            public void onFailure(Call<EvolutionChainResponse> call, Throwable t) {
                showNoEvolution();
            }
        });
    }

    private void renderEvolutionChain(EvolutionChainResponse data) {
        layoutEvolution.removeAllViews();
        List<String> evoNames = new ArrayList<>();

        EvolutionChainResponse.ChainLink current = data.getChain();
        while (current != null) {
            evoNames.add(current.getSpecies().getName());
            if (current.getEvolvesTo() != null && !current.getEvolvesTo().isEmpty()) {
                current = current.getEvolvesTo().get(0);
            } else {
                current = null;
            }
        }

        if (evoNames.isEmpty()) {
            showNoEvolution();
            return;
        }

        for (int i = 0; i < evoNames.size(); i++) {
            String name = evoNames.get(i);

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setGravity(Gravity.CENTER);
            itemLayout.setPadding(16, 0, 16, 0);

            ImageView iv = new ImageView(this);
            int size = 150;
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(size, size);
            iv.setLayoutParams(imgParams);
            iv.setImageResource(R.drawable.ic_launcher_foreground);

            TextView tv = new TextView(this);
            tv.setText(name);
            tv.setGravity(Gravity.CENTER);

            itemLayout.addView(iv);
            itemLayout.addView(tv);
            layoutEvolution.addView(itemLayout);

            if (i < evoNames.size() - 1) {
                ImageView arrow = new ImageView(this);
                arrow.setImageResource(android.R.drawable.ic_media_play);
                arrow.setColorFilter(Color.GRAY);
                layoutEvolution.addView(arrow);
            }
        }
    }

    private void showNoEvolution() {
        TextView tv = new TextView(this);
        tv.setText("No evolution chain exist");
        tv.setTextColor(Color.GRAY);
        layoutEvolution.removeAllViews();
        layoutEvolution.addView(tv);
    }
}