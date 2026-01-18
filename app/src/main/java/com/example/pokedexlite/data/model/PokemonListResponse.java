package com.example.pokedexlite.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PokemonListResponse {
    @SerializedName("results")
    private List<PokemonResult> results;

    public List<PokemonResult> getResults() {
        return results;
    }

    public static class PokemonResult {
        @SerializedName("name")
        private String name;
        @SerializedName("url")
        private String url;

        public String getName() { return name; }
        public String getUrl() { return url; }

        public int getId() {
            String[] parts = url.split("/");
            return Integer.parseInt(parts[parts.length - 1]);
        }

        public String getImageUrl() {
            return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + getId() + ".png";
        }
    }
}