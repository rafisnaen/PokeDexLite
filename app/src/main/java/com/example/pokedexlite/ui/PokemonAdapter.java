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

        holder.tvName.setText(pokemon.getName());
        Picasso.get()
                .load(pokemon.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.imgPokemon);
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("EXTRA_ID", pokemon.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return (pokemonList != null) ? pokemonList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPokemon;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPokemon = itemView.findViewById(R.id.imgPokemon);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}