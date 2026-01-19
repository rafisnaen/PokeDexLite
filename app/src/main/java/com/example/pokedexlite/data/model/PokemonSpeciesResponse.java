package com.example.pokedexlite.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PokemonSpeciesResponse {
    @SerializedName("flavor_text_entries")
    private List<FlavorTextEntry> flavorTextEntries;

    @SerializedName("evolution_chain")
    private EvolutionChainUrl evolutionChain;

    public List<FlavorTextEntry> getFlavorTextEntries() {
        return flavorTextEntries;
    }

    public EvolutionChainUrl getEvolutionChain() {
        return evolutionChain;
    }

    public static class FlavorTextEntry {
        @SerializedName("flavor_text")
        private String flavorText;
        @SerializedName("language")
        private NamedResource language;

        public String getFlavorText() { return flavorText; }
        public NamedResource getLanguage() { return language; }
    }

    public static class EvolutionChainUrl {
        @SerializedName("url")
        private String url;
        public String getUrl() { return url; }
    }

    public static class NamedResource {
        @SerializedName("name")
        private String name;
        public String getName() { return name; }
    }
}