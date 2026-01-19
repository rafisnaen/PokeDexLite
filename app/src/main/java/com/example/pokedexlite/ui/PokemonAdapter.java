package com.example.pokedexlite.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokedexlite.R; // Pastikan R diimport
import com.example.pokedexlite.data.model.PokemonListResponse;
import com.example.pokedexlite.databinding.ItemPokemonBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PokemonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    private List<PokemonListResponse.PokemonResult> pokemonList;
    private Context context;
    private OnItemClickListener listener;
    private OnLoadMoreListener loadMoreListener;

    private String nextGenLabel = "Load Next Gen";
    private boolean isFooterEnabled = true;

    public interface OnItemClickListener {
        void onItemClick(PokemonListResponse.PokemonResult pokemon);
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public PokemonAdapter(Context context, OnItemClickListener listener, OnLoadMoreListener loadMoreListener) {
        this.context = context;
        this.listener = listener;
        this.loadMoreListener = loadMoreListener;
        this.pokemonList = new ArrayList<>();
    }

    public void setPokemonList(List<PokemonListResponse.PokemonResult> list) {
        this.pokemonList = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public void addPokemonList(List<PokemonListResponse.PokemonResult> newList) {
        int startPos = this.pokemonList.size();
        this.pokemonList.addAll(newList);
        notifyItemRangeInserted(startPos, newList.size());
    }

    public void setNextGenLabel(String label) {
        this.nextGenLabel = label;
        if (!pokemonList.isEmpty() && isFooterEnabled) {
            notifyItemChanged(pokemonList.size());
        }
    }

    public void setFooterEnabled(boolean enabled) {
        this.isFooterEnabled = enabled;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == pokemonList.size() && isFooterEnabled) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        if (pokemonList.isEmpty()) return 0;
        return isFooterEnabled ? pokemonList.size() + 1 : pokemonList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            frameLayout.setPadding(32, 32, 32, 64);

            Button btnLoad = new Button(context);
            btnLoad.setText("LOAD MORE");
            btnLoad.setBackgroundResource(R.drawable.bg_button_rounded);
            btnLoad.setTextColor(Color.WHITE);

            btnLoad.setTextSize(16);
            btnLoad.setPadding(30, 20, 30, 20);

            FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            btnParams.gravity = Gravity.CENTER;
            btnLoad.setLayoutParams(btnParams);
            frameLayout.addView(btnLoad);

            return new FooterViewHolder(frameLayout, btnLoad);
        } else {
            ItemPokemonBinding binding = ItemPokemonBinding.inflate(LayoutInflater.from(context), parent, false);
            return new PokemonViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_FOOTER) {
            FooterViewHolder footerHolder = (FooterViewHolder) holder;
            footerHolder.btnLoad.setText(nextGenLabel);
            footerHolder.btnLoad.setOnClickListener(v -> {
                if (loadMoreListener != null) loadMoreListener.onLoadMore();
            });
        } else {
            PokemonViewHolder pokemonHolder = (PokemonViewHolder) holder;
            if (position < pokemonList.size()) {
                PokemonListResponse.PokemonResult pokemon = pokemonList.get(position);
                pokemonHolder.bind(pokemon, listener);
            }
        }
    }

    static class PokemonViewHolder extends RecyclerView.ViewHolder {
        ItemPokemonBinding binding;

        public PokemonViewHolder(ItemPokemonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PokemonListResponse.PokemonResult pokemon, OnItemClickListener listener) {
            binding.tvName.setText(pokemon.getName());
            String id = getPokemonIdFromUrl(pokemon.getUrl());
            binding.tvId.setText("#" + id);
            binding.tvType.setVisibility(View.GONE);

            String imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + id + ".png";
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(binding.ivPokemon);

            itemView.setOnClickListener(v -> listener.onItemClick(pokemon));
        }

        private String getPokemonIdFromUrl(String url) {
            if (url == null) return "0";
            if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
            String[] parts = url.split("/");
            return parts[parts.length - 1];
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        Button btnLoad;
        public FooterViewHolder(@NonNull View itemView, Button btn) {
            super(itemView);
            this.btnLoad = btn;
        }
    }
}