package com.example.pokedexlite.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Pastikan R di-import dari package aplikasi Anda
import com.example.pokedexlite.R;
import com.example.pokedexlite.data.local.DatabaseHelper;
import com.example.pokedexlite.data.model.PokemonListResponse;
import com.example.pokedexlite.data.remote.PokeApiService;
import com.example.pokedexlite.data.remote.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PokemonAdapter adapter;
    private PokeApiService apiService;
    private DatabaseHelper dbHelper;
    private EditText etSearch;
    private View loadingOverlay;
    private Toolbar toolbar;

    private List<PokemonListResponse.PokemonResult> currentBrowseList = new ArrayList<>();
    private List<PokemonListResponse.PokemonResult> masterSearchList = new ArrayList<>();

    private int currentGenIndex = 0;
    private final int[] genOffsets = {0, 151, 251, 386, 493, 649, 721, 809, 905};
    private final int[] genLimits = {151, 100, 135, 107, 156, 72, 88, 96, 120};
    private final String[] genLabels = {
            "Load Gen 2 (Johto)", "Load Gen 3 (Hoenn)", "Load Gen 4 (Sinnoh)",
            "Load Gen 5 (Unova)", "Load Gen 6 (Kalos)", "Load Gen 7 (Alola)",
            "Load Gen 8 (Galar)", "Load Gen 9 (Paldea)"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = RetrofitClient.getClient().create(PokeApiService.class);
        dbHelper = new DatabaseHelper(this);

        initViews();
        setupRecyclerView();
        setupSearch();

        loadPokemonGen(currentGenIndex);
        loadGlobalSearchData();

        hideSystemUI();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        etSearch = findViewById(R.id.et_search);
        loadingOverlay = findViewById(R.id.progressBar);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemViewType(position) == 1 ? 2 : 1;
            }
        });

        recyclerView.setLayoutManager(layoutManager);

        adapter = new PokemonAdapter(this, pokemon -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("EXTRA_NAME", pokemon.getName());
            startActivity(intent);
        }, this::loadNextGeneration);

        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();

                if (query.isEmpty()) {
                    adapter.setPokemonList(currentBrowseList);
                    boolean isMaxGen = currentGenIndex >= genOffsets.length;
                    adapter.setFooterEnabled(!isMaxGen);
                } else {
                    List<PokemonListResponse.PokemonResult> filteredList = new ArrayList<>();
                    List<PokemonListResponse.PokemonResult> sourceList =
                            masterSearchList.isEmpty() ? currentBrowseList : masterSearchList;

                    for (PokemonListResponse.PokemonResult p : sourceList) {
                        if (p.getName().toLowerCase().contains(query)) {
                            filteredList.add(p);
                        }
                    }
                    adapter.setPokemonList(filteredList);
                    adapter.setFooterEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadGlobalSearchData() {
        apiService.getPokemonList(2000, 0).enqueue(new Callback<PokemonListResponse>() {
            @Override
            public void onResponse(Call<PokemonListResponse> call, Response<PokemonListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    masterSearchList = response.body().getResults();
                }
            }
            @Override
            public void onFailure(Call<PokemonListResponse> call, Throwable t) {}
        });
    }

    private void loadPokemonGen(int genIndex) {
        if (genIndex >= genOffsets.length) return;

        showLoading(true);
        int limit = genLimits[genIndex];
        int offset = genOffsets[genIndex];

        if (genIndex < genLabels.length) {
            adapter.setNextGenLabel(genLabels[genIndex]);
        } else {
            adapter.setFooterEnabled(false);
        }

        apiService.getPokemonList(limit, offset).enqueue(new Callback<PokemonListResponse>() {
            @Override
            public void onResponse(Call<PokemonListResponse> call, Response<PokemonListResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<PokemonListResponse.PokemonResult> newResults = response.body().getResults();

                    if (genIndex == 0) {
                        currentBrowseList = new ArrayList<>(newResults);
                        adapter.setPokemonList(currentBrowseList);
                    } else {
                        currentBrowseList.addAll(newResults);
                        adapter.addPokemonList(newResults);
                        Toast.makeText(MainActivity.this, "Gen " + (genIndex + 1) + " Loaded!", Toast.LENGTH_SHORT).show();
                    }

                    String json = new Gson().toJson(response.body());
                    dbHelper.saveCache("gen_" + genIndex, json);
                }
            }

            @Override
            public void onFailure(Call<PokemonListResponse> call, Throwable t) {
                showLoading(false);
                String cachedJson = dbHelper.getCache("gen_" + genIndex);
                if (cachedJson != null) {
                    PokemonListResponse cachedData = new Gson().fromJson(cachedJson, PokemonListResponse.class);
                    if (genIndex == 0) {
                        currentBrowseList = new ArrayList<>(cachedData.getResults());
                        adapter.setPokemonList(currentBrowseList);
                    } else {
                        currentBrowseList.addAll(cachedData.getResults());
                        adapter.addPokemonList(cachedData.getResults());
                    }
                    Toast.makeText(MainActivity.this, "Loaded from Cache (Offline)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadNextGeneration() {
        currentGenIndex++;
        if (currentGenIndex < genOffsets.length) {
            loadPokemonGen(currentGenIndex);
        } else {
            adapter.setFooterEnabled(false);
        }
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_team) {
            startActivity(new Intent(this, TeamActivity.class));
            return true;
        } else if (id == R.id.action_favorites) {
            startActivity(new Intent(this, FavoritesActivity.class));
            return true;
        } else if (id == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideSystemUI() {
        androidx.core.view.WindowInsetsControllerCompat windowInsetsController =
                androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setSystemBarsBehavior(
                    androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
            windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.statusBars());
        }
    }
}