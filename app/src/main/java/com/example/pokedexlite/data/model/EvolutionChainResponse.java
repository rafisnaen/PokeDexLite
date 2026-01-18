package com.example.pokedexlite.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EvolutionChainResponse {
    @SerializedName("chain")
    private ChainLink chain;

    public ChainLink getChain() {
        return chain;
    }
    
    public static class ChainLink {
        @SerializedName("species")
        private NamedResource species;

        @SerializedName("evolves_to")
        private List<ChainLink> evolvesTo;

        public String getSpeciesName() {
            return species != null ? species.name : "";
        }

        public List<ChainLink> getEvolvesTo() {
            return evolvesTo;
        }

        public int getSpeciesId() {
            if (species == null || species.url == null) return 0;
            String[] parts = species.url.split("/");
            return Integer.parseInt(parts[parts.length - 1]);
        }
    }

    private static class NamedResource {
        @SerializedName("name")
        String name;
        @SerializedName("url")
        String url;
    }
}