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

        private final TextView tvRoute;
        private final TextView tvDate;
        private final TextView tvPrice;
        private final TextView tvAirline;
        private final TextView tvTimes;
        private final OnItemClickListener listener;

        SearchViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
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
            if (tvAirline != null) {
                if (airline != null && !airline.isEmpty()) {
                    tvAirline.setText(airline);
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
    }
}
