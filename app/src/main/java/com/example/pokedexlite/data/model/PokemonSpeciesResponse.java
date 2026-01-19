package com.example.pokedexlite.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PokemonSpeciesResponse {

    @SerializedName("evolution_chain")
    private EvolutionChainUrl evolutionChain;

    @SerializedName("flavor_text_entries")
    private List<FlavorTextEntry> flavorTextEntries;

    public String getEvolutionChainUrl() {
        return evolutionChain != null ? evolutionChain.url : null;
    }

    public String getDescription() {
        if (flavorTextEntries != null && !flavorTextEntries.isEmpty()) {
            for (FlavorTextEntry entry : flavorTextEntries) {
                if ("en".equals(entry.language.name)) {
                    return entry.flavorText.replace("\n", " ").replace("\f", " ");
                }
            }
        }
        return "No description provided for this pokemon";
    }

    private static class EvolutionChainUrl {
        @SerializedName("url")
        String url;
    }

    private static class FlavorTextEntry {
        @SerializedName("flavor_text")
        String flavorText;
        @SerializedName("language")
        NamedResource language;
    }

    private static class NamedResource {
        @SerializedName("name")
        String name;
    }
}