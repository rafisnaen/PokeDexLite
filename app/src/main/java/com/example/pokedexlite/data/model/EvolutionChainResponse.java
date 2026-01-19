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

        public NamedResource getSpecies() { return species; }
        public List<ChainLink> getEvolvesTo() { return evolvesTo; }
    }

    public static class NamedResource {
        @SerializedName("name")
        private String name;
        @SerializedName("url")
        private String url;

        public String getName() { return name; }
        public String getUrl() { return url; }
    }
}