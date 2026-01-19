package com.example.pokedexlite.data.remote;

import com.example.pokedexlite.data.model.EvolutionChainResponse;
import com.example.pokedexlite.data.model.PokemonDetailResponse;
import com.example.pokedexlite.data.model.PokemonListResponse;
import com.example.pokedexlite.data.model.PokemonSpeciesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface PokeApiService {
    @GET("pokemon")
    Call<PokemonListResponse> getPokemonList(
            @Query("limit") int limit,
            @Query("offset") int offset
    );
    @GET("pokemon/{id}")
    Call<PokemonDetailResponse> getPokemonDetail(@Path("id") String nameOrId);
    @GET("pokemon-species/{id}")
    Call<PokemonSpeciesResponse> getPokemonSpecies(@Path("id") int id);
    @GET
    Call<EvolutionChainResponse> getEvolutionChain(@Url String url);
}