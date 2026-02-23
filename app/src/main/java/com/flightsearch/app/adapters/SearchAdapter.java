package com.flightsearch.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.flightsearch.app.R;
import com.flightsearch.app.models.FlightSearch;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<FlightSearch> searches = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FlightSearch search);
        void onItemLongClick(FlightSearch search);
    }

    public SearchAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_search, parent, false);
        return new SearchViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        holder.bind(searches.get(position));
    }

    @Override
    public int getItemCount() {
        return searches.size();
    }

    public void submitList(List<FlightSearch> newSearches) {
        this.searches = newSearches != null ? newSearches : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewRoute;
        private final TextView textViewDate;
        private final TextView textViewPrice;
        private final OnItemClickListener listener;

        public SearchViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            textViewRoute = itemView.findViewById(R.id.textViewRoute);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
        }

        public void bind(FlightSearch search) {
            if (search != null) {
                String from = search.getFromCity();
                String to = search.getToCity();
                String route = from + " → " + to;
                textViewRoute.setText(route);

                String date = search.getDepartureDate();
                textViewDate.setText(date);

                String price = search.getPrice();
                if (price != null) {
                    if (!price.startsWith("$")) {
                        price = "$" + price;
                    }
                } else {
                    price = "";
                }
                textViewPrice.setText(price);

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(search);
                    }
                });

                itemView.setOnLongClickListener(v -> {
                    if (listener != null) {
                        listener.onItemLongClick(search);
                        return true;
                    }
                    return false;
                });
            }
        }
    }
}