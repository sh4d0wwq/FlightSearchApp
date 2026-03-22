package com.flightsearch.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.flightsearch.app.R;
import com.flightsearch.app.models.FlightOffer;

public class FlightOfferAdapter extends ListAdapter<FlightOffer, FlightOfferAdapter.OfferViewHolder> {

    public interface OnOfferClickListener {
        void onOfferClick(FlightOffer offer);
    }

    private final OnOfferClickListener listener;

    public FlightOfferAdapter(OnOfferClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<FlightOffer> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<FlightOffer>() {
                @Override
                public boolean areItemsTheSame(@NonNull FlightOffer a, @NonNull FlightOffer b) {
                    return a.getOfferId() != null && a.getOfferId().equals(b.getOfferId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull FlightOffer a, @NonNull FlightOffer b) {
                    return a.getOfferId() != null && a.getOfferId().equals(b.getOfferId())
                            && a.getPrice() != null && a.getPrice().equals(b.getPrice());
                }
            };

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flight_offer, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class OfferViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvAirline;
        private final TextView tvFlightNumber;
        private final TextView tvRoute;
        private final TextView tvDepTime;
        private final TextView tvArrTime;
        private final TextView tvDuration;
        private final TextView tvStops;
        private final TextView tvPrice;

        OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAirline = itemView.findViewById(R.id.tvOfferAirline);
            tvFlightNumber = itemView.findViewById(R.id.tvOfferFlightNumber);
            tvRoute = itemView.findViewById(R.id.tvOfferRoute);
            tvDepTime = itemView.findViewById(R.id.tvOfferDepTime);
            tvArrTime = itemView.findViewById(R.id.tvOfferArrTime);
            tvDuration = itemView.findViewById(R.id.tvOfferDuration);
            tvStops = itemView.findViewById(R.id.tvOfferStops);
            tvPrice = itemView.findViewById(R.id.tvOfferPrice);
        }

        void bind(FlightOffer offer, OnOfferClickListener listener) {
            String airlineName = offer.getAirlineName() != null ? offer.getAirlineName() : offer.getAirline();
            tvAirline.setText(airlineName != null ? airlineName : "");
            tvFlightNumber.setText(offer.getFlightNumber() != null ? offer.getFlightNumber() : "");

            String from = offer.getFromCode() != null ? offer.getFromCode() : offer.getFromCity();
            String to = offer.getToCode() != null ? offer.getToCode() : offer.getToCity();
            tvRoute.setText(from + " → " + to);

            tvDepTime.setText(nvl(offer.getDepartureTime(), "--:--"));
            tvArrTime.setText(nvl(offer.getArrivalTime(), "--:--"));
            tvDuration.setText(nvl(offer.getDuration(), ""));

            int stops = offer.getStops();
            if (stops == 0) {
                tvStops.setText(itemView.getContext().getString(R.string.stops_direct));
            } else if (stops == 1) {
                tvStops.setText(itemView.getContext().getString(R.string.stops_one));
            } else {
                tvStops.setText(itemView.getContext().getString(R.string.stops_format, stops));
            }

            tvPrice.setText(offer.getFormattedPrice());

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onOfferClick(offer);
            });
        }

        private String nvl(String s, String fallback) {
            return (s != null && !s.isEmpty()) ? s : fallback;
        }
    }
}
