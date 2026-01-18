package com.example.pokedexlite.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.pokedexlite.data.local.DatabaseHelper;
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
        dbHelper = new DatabaseHelper(this);
        apiService = RetrofitClient.getService();
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        loadPokemonData();
    }
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}