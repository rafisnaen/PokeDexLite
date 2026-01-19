package com.example.pokedexlite.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokedexlite.R;
import com.example.pokedexlite.data.model.PokemonListResponse;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.ViewHolder> {

    private List<PokemonListResponse.PokemonResult> pokemonList;

    public PokemonAdapter(List<PokemonListResponse.PokemonResult> pokemonList) {
        this.pokemonList = pokemonList;
    }

    public void setFilteredList(List<PokemonListResponse.PokemonResult> filteredList) {
        this.pokemonList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pokemon, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PokemonListResponse.PokemonResult pokemon = pokemonList.get(position);
        String name = pokemon.getName();
        if (name != null && !name.isEmpty()) {
            holder.tvName.setText(name.substring(0, 1).toUpperCase() + name.substring(1));
        }

        String pokemonId = getPokemonIdFromUrl(pokemon.getUrl());
        holder.tvId.setText("#" + pokemonId);
        String imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + pokemonId + ".png";

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.ivPokemon);

        holder.tvType.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("EXTRA_NAME", pokemon.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return (pokemonList != null) ? pokemonList.size() : 0;
    }

    private String getPokemonIdFromUrl(String url) {
        if (url == null) return "";
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPokemon;
        TextView tvName;
        TextView tvId;
        TextView tvType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPokemon = itemView.findViewById(R.id.iv_pokemon);
            tvName = itemView.findViewById(R.id.tv_name);
            tvId = itemView.findViewById(R.id.tv_id);
            tvType = itemView.findViewById(R.id.tv_type);
        }
    }
}