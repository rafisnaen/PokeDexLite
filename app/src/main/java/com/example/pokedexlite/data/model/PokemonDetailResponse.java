package com.example.pokedexlite.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PokemonDetailResponse {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("height")
    private int height;
    @SerializedName("weight")
    private int weight;
    @SerializedName("types")
    private List<TypeSlot> types;
    @SerializedName("stats")
    private List<StatSlot> stats;

    @SerializedName("abilities")
    private List<AbilitySlot> abilities;
    public int getId() { return id; }
    public String getName() { return name; }
    public int getHeight() { return height; }
    public int getWeight() { return weight; }
    public List<TypeSlot> getTypes() { return types; }
    public List<StatSlot> getStats() { return stats; }
    public List<AbilitySlot> getAbilities() { return abilities; }

    public static class TypeSlot {
        @SerializedName("type")
        private NamedResource type;
        public NamedResource getType() { return type; }
    }

    public static class StatSlot {
        @SerializedName("base_stat")
        private int baseStat;
        @SerializedName("stat")
        private NamedResource stat;

        public int getBaseStat() { return baseStat; }
        public NamedResource getStat() { return stat; }
    }

    public static class AbilitySlot {
        @SerializedName("ability")
        private NamedResource ability;

        public NamedResource getAbility() { return ability; }
    }

    public static class NamedResource {
        @SerializedName("name")
        private String name;
        public String getName() { return name; }
    }
}