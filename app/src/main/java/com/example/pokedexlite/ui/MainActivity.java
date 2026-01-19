package com.example.pokedexlite.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.pokedexlite.R;
import com.example.pokedexlite.data.local.DatabaseHelper;
import com.example.pokedexlite.data.model.PokemonListResponse;
import com.example.pokedexlite.data.remote.PokeApiService;
import com.example.pokedexlite.data.remote.RetrofitClient;
import com.example.pokedexlite.databinding.ActivityMainBinding;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DatabaseHelper dbHelper;
    private PokeApiService apiService;
    private List<PokemonListResponse.PokemonResult> masterPokemonList = new ArrayList<>();
    private List<PokemonListResponse.PokemonResult> displayedList = new ArrayList<>();

    private PokemonAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        dbHelper = new DatabaseHelper(this);
        apiService = RetrofitClient.getService();
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new PokemonAdapter(displayedList);
        binding.recyclerView.setAdapter(adapter);

        hideSystemUI();
        loadMasterData();
    }

    private void loadMasterData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        Call<PokemonListResponse> call = apiService.getPokemonList(2000, 0);

        call.enqueue(new Callback<PokemonListResponse>() {
            @Override
            public void onResponse(Call<PokemonListResponse> call, Response<PokemonListResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    masterPokemonList.clear();
                    masterPokemonList.addAll(response.body().getResults());
                    String json = new Gson().toJson(response.body());
                    dbHelper.saveCache("master_list", json);
                    showDefaultList();
                }
            }

            @Override
            public void onFailure(Call<PokemonListResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Offline Mode / Error", Toast.LENGTH_SHORT).show();
                String cachedJson = dbHelper.getCache("master_list");
                if (cachedJson != null) {
                    PokemonListResponse cachedResponse = new Gson().fromJson(cachedJson, PokemonListResponse.class);
                    masterPokemonList.clear();
                    masterPokemonList.addAll(cachedResponse.getResults());
                    showDefaultList();
                } else {
                    Toast.makeText(MainActivity.this, "No Cache Available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void showDefaultList() {
        displayedList.clear();
        if (masterPokemonList.size() >= 151) {
            displayedList.addAll(masterPokemonList.subList(0, 151));
        } else {
            displayedList.addAll(masterPokemonList);
        }
        adapter = new PokemonAdapter(displayedList);
        binding.recyclerView.setAdapter(adapter);
    }
    private void filterList(String query) {
        if (query.isEmpty()) {
            showDefaultList();
            return;
        }

        List<PokemonListResponse.PokemonResult> filtered = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        for (PokemonListResponse.PokemonResult item : masterPokemonList) {
            if (item.getName().toLowerCase().contains(lowerCaseQuery)) {
                filtered.add(item);
            }
        }
        adapter = new PokemonAdapter(filtered);
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        android.view.MenuItem searchItem = menu.findItem(R.id.action_search);
        android.view.MenuItem historyItem = menu.findItem(R.id.action_history);
        historyItem.setVisible(false);

        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search (e.g., 'mew')...");
        searchItem.setOnActionExpandListener(new android.view.MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(android.view.MenuItem item) {
                historyItem.setVisible(true);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(android.view.MenuItem item) {
                historyItem.setVisible(false);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
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

    private void hideSystemUI() {
        androidx.core.view.WindowInsetsControllerCompat windowInsetsController =
                androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(
                androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.statusBars());
    }
}