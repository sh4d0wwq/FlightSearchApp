package com.flightsearch.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.flightsearch.app.models.FlightOffer;
import com.flightsearch.app.network.NetworkMonitor;
import com.flightsearch.app.repository.FlightRepository;

import java.util.ArrayList;
import java.util.List;

public class FlightViewModel extends AndroidViewModel {

    private final FlightRepository repository;

    public final MutableLiveData<List<FlightOffer>> flightOffers = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<Boolean> isLoading  = new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> isOffline  = new MutableLiveData<>(false);
    public final MutableLiveData<FlightRepository.DataSource> dataSource = new MutableLiveData<>();
    public final MutableLiveData<Boolean> hasResults = new MutableLiveData<>(false);
    public final MutableLiveData<String>  errorEvent = new MutableLiveData<>();

    public FlightViewModel(@NonNull Application application) {
        super(application);
        repository = new FlightRepository();
    }

    public void checkNetworkStatus() {
        isOffline.setValue(!NetworkMonitor.isNetworkAvailable(getApplication()));
    }

    public void searchFlights(String fromCity, String toCity, String date) {
        isLoading.setValue(true);
        dataSource.setValue(null);
        errorEvent.setValue(null);

        repository.searchFlights(fromCity, toCity, date, new FlightRepository.SearchCallback() {
            @Override
            public void onSuccess(List<FlightOffer> offers, FlightRepository.DataSource src) {
                isLoading.setValue(false);
                flightOffers.setValue(offers);
                dataSource.setValue(src);
                hasResults.setValue(!offers.isEmpty());
                if (offers.isEmpty()) errorEvent.setValue("no_results");
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                hasResults.setValue(false);
                errorEvent.setValue(message);
            }
        });
    }

    public void clearResults() {
        flightOffers.setValue(new ArrayList<>());
        hasResults.setValue(false);
        errorEvent.setValue(null);
        dataSource.setValue(null);
    }
}
