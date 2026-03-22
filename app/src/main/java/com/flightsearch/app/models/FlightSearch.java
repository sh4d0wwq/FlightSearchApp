package com.flightsearch.app.models;

import java.util.Objects;

public class FlightSearch {

    private long id;
    private String fromCity;
    private String toCity;
    private String departureDate;
    private String price;

    private String airline;
    private String airlineName;
    private String flightNumber;
    private String departureTime;
    private String arrivalTime;
    private String duration;
    private int stops;

    public FlightSearch() {}

    public FlightSearch(String fromCity, String toCity, String departureDate, String price) {
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.departureDate = departureDate;
        this.price = price;
    }

    public FlightSearch(long id, String fromCity, String toCity, String departureDate, String price) {
        this.id = id;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.departureDate = departureDate;
        this.price = price;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFromCity() { return fromCity; }
    public void setFromCity(String fromCity) { this.fromCity = fromCity; }

    public String getToCity() { return toCity; }
    public void setToCity(String toCity) { this.toCity = toCity; }

    public String getDepartureDate() { return departureDate; }
    public void setDepartureDate(String departureDate) { this.departureDate = departureDate; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }

    public String getAirlineName() { return airlineName; }
    public void setAirlineName(String airlineName) { this.airlineName = airlineName; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public int getStops() { return stops; }
    public void setStops(int stops) { this.stops = stops; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlightSearch that = (FlightSearch) o;
        return id == that.id &&
                Objects.equals(fromCity, that.fromCity) &&
                Objects.equals(toCity, that.toCity) &&
                Objects.equals(departureDate, that.departureDate) &&
                Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fromCity, toCity, departureDate, price);
    }

    @Override
    public String toString() {
        return "FlightSearch{id=" + id +
                ", from='" + fromCity + "', to='" + toCity + "'" +
                ", date='" + departureDate + "', price='" + price + "'}";
    }
}
