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

        // Setup ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Init Database
        dbHelper = new DatabaseHelper(this);

        // Init API
        apiService = RetrofitClient.getService();

        // Setup RecyclerView
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        loadPokemonData();
    }

    private void loadPokemonData() {
        // Cek Cache dulu (Logic Offline Mode) [cite: 99]
        // Untuk simplifikasi, kita panggil API langsung disini.
        // Idealnya: Check Connection -> if connected (API & Save Cache) -> else (Load Cache)

        Call<PokemonListResponse> call = apiService.getPokemonList(20, 0); // Limit 20 [cite: 49]

        call.enqueue(new Callback<PokemonListResponse>() {
            @Override
            public void onResponse(Call<PokemonListResponse> call, Response<PokemonListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Tampilkan Data
                    PokemonAdapter adapter = new PokemonAdapter(response.body().getResults());
                    binding.recyclerView.setAdapter(adapter);

                    // SIMPAN CACHE (Contoh implementasi) [cite: 98]
                    String json = new Gson().toJson(response.body());
                    dbHelper.saveCache("home_list", json);
                }
            }

            @Override
            public void onFailure(Call<PokemonListResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network Error. Loading Cache...", Toast.LENGTH_SHORT).show();

                // Load dari Cache jika error/offline [cite: 70]
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