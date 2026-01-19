package com.example.pokedexlite.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.pokedexlite.R;
import com.example.pokedexlite.data.local.DatabaseHelper;
import com.example.pokedexlite.data.model.PokemonDetailResponse;
import com.example.pokedexlite.data.model.PokemonListResponse;
import com.example.pokedexlite.data.remote.PokeApiService;
import com.example.pokedexlite.data.remote.RetrofitClient;
import com.example.pokedexlite.databinding.ActivityMainBinding;
import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DatabaseHelper dbHelper;
    private PokeApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        dbHelper = new DatabaseHelper(this);
        apiService = RetrofitClient.getService();
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        loadPokemonData();
        hideSystemUI();
    }
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        android.view.MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("Search Pokemon Name/ID...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()) {
                    performSearch(query.trim().toLowerCase());
                    searchView.clearFocus();
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_favorites) {
            startActivity(new android.content.Intent(this, FavoritesActivity.class));
            return true;
        } else if (id == R.id.action_team) {
            startActivity(new android.content.Intent(this, TeamActivity.class));
            return true;
        } else if (id == R.id.action_history) {
            startActivity(new android.content.Intent(this, HistoryActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void loadPokemonData() {
        Call<PokemonListResponse> call = apiService.getPokemonList(20, 0);

        call.enqueue(new Callback<PokemonListResponse>() {
            @Override
            public void onResponse(Call<PokemonListResponse> call, Response<PokemonListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PokemonAdapter adapter = new PokemonAdapter(response.body().getResults());
                    binding.recyclerView.setAdapter(adapter);

                    String json = new Gson().toJson(response.body());
                    dbHelper.saveCache("home_list", json);
                }
            }

            @Override
            public void onFailure(Call<PokemonListResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network Error. Loading Cache...", Toast.LENGTH_SHORT).show();

                String cachedJson = dbHelper.getCache("home_list");
                if (cachedJson != null) {
                    PokemonListResponse cachedResponse = new Gson().fromJson(cachedJson, PokemonListResponse.class);
                    PokemonAdapter adapter = new PokemonAdapter(cachedResponse.getResults());
                    binding.recyclerView.setAdapter(adapter);
                }
            }
        });
    }
    private void performSearch(String query) {
        binding.progressBar.setVisibility(android.view.View.VISIBLE);
        apiService.getPokemonDetail(query).enqueue(new Callback<PokemonDetailResponse>() {
            @Override
            public void onResponse(Call<PokemonDetailResponse> call, Response<PokemonDetailResponse> response) {
                binding.progressBar.setVisibility(android.view.View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    PokemonDetailResponse data = response.body();
                    dbHelper.addHistory(query, data.getId(), data.getName());
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra("EXTRA_ID", data.getId());
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Pokemon Not Found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PokemonDetailResponse> call, Throwable t) {
                binding.progressBar.setVisibility(android.view.View.GONE);
                Toast.makeText(MainActivity.this, "Search Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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