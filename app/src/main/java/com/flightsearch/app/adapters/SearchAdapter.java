package com.flightsearch.app.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.flightsearch.app.R;
import com.flightsearch.app.models.FlightSearch;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<FlightSearch> flights = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FlightSearch flight);
        void onItemLongClick(FlightSearch flight);
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
        holder.bind(flights.get(position));
    }

    @Override
    public int getItemCount() {
        return flights.size();
    }

    public void submitList(List<FlightSearch> newFlights) {
        this.flights = newFlights != null ? newFlights : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivThumb;
        private final TextView tvRoute;
        private final TextView tvDate;
        private final TextView tvPrice;
        private final TextView tvAirline;
        private final TextView tvTimes;
        private final OnItemClickListener listener;

        SearchViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            ivThumb = itemView.findViewById(R.id.ivThumb);
            tvRoute = itemView.findViewById(R.id.textViewRoute);
            tvDate = itemView.findViewById(R.id.textViewDate);
            tvPrice = itemView.findViewById(R.id.textViewPrice);
            tvAirline = itemView.findViewById(R.id.textViewAirline);
            tvTimes = itemView.findViewById(R.id.textViewTimes);
        }

        void bind(FlightSearch flight) {
            if (flight == null) return;

            tvRoute.setText(flight.getFromCity() + " → " + flight.getToCity());
            tvDate.setText(flight.getDepartureDate() != null ? flight.getDepartureDate() : "");
            tvPrice.setText(flight.getPrice() != null ? flight.getPrice() : "");

            String airline = flight.getAirlineName();
            if (airline == null || airline.isEmpty()) airline = flight.getAirline();
            String cat = flight.getTripCategory();
            if (tvAirline != null) {
                StringBuilder line = new StringBuilder();
                if (airline != null && !airline.isEmpty()) line.append(airline);
                if (cat != null && !cat.isEmpty()) {
                    if (line.length() > 0) line.append(" · ");
                    line.append(labelCategory(itemView, cat));
                }
                if (line.length() > 0) {
                    tvAirline.setText(line.toString());
                    tvAirline.setVisibility(View.VISIBLE);
                } else {
                    tvAirline.setVisibility(View.GONE);
                }
            }

            if (tvTimes != null) {
                String dep = flight.getDepartureTime();
                String arr = flight.getArrivalTime();
                if (dep != null && !dep.isEmpty() && arr != null && !arr.isEmpty()) {
                    tvTimes.setText(dep + " → " + arr);
                    tvTimes.setVisibility(View.VISIBLE);
                } else {
                    tvTimes.setVisibility(View.GONE);
                }
            }

            String url = flight.getImageUrl();
            if (url != null && !url.isEmpty()) {
                ivThumb.setImageTintList(null);
                int p = (int) (2 * itemView.getResources().getDisplayMetrics().density);
                ivThumb.setPadding(p, p, p, p);
                Glide.with(itemView.getContext())
                        .load(url)
                        .circleCrop()
                        .placeholder(R.drawable.ic_bookmark)
                        .into(ivThumb);
            } else {
                Glide.with(itemView.getContext()).clear(ivThumb);
                ivThumb.setImageResource(R.drawable.ic_bookmark);
                int p = (int) (10 * itemView.getResources().getDisplayMetrics().density);
                ivThumb.setPadding(p, p, p, p);
                ivThumb.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.getContext(), R.color.primary_color)));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(flight);
            });
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onItemLongClick(flight);
                    return true;
                }
                return false;
            });
        }

        private static String labelCategory(View v, String key) {
            if ("business".equalsIgnoreCase(key)) return v.getContext().getString(R.string.cat_business);
            if ("family".equalsIgnoreCase(key)) return v.getContext().getString(R.string.cat_family);
            if ("leisure".equalsIgnoreCase(key)) return v.getContext().getString(R.string.cat_leisure);
            return key;
        }
    }
}
