package com.flightsearch.app.models;

public class FlightOffer {

    private String offerId;
    private String airline;
    private String airlineName;
    private String flightNumber;
    private String fromCity;
    private String fromCode;
    private String toCity;
    private String toCode;
    private String departureDate;
    private String departureTime;
    private String arrivalTime;
    private String duration;
    private int stops;
    private String price;
    private String currency;

    public FlightOffer() {}

    public String getOfferId() { return offerId; }
    public String getAirline() { return airline; }
    public String getAirlineName() { return airlineName; }
    public String getFlightNumber() { return flightNumber; }
    public String getFromCity() { return fromCity; }
    public String getFromCode() { return fromCode; }
    public String getToCity() { return toCity; }
    public String getToCode() { return toCode; }
    public String getDepartureDate() { return departureDate; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public String getDuration() { return duration; }
    public int getStops() { return stops; }
    public String getPrice() { return price; }
    public String getCurrency() { return currency; }

    public String getFormattedPrice() {
        if (price == null) return "N/A";
        String cur = currency != null ? currency : "USD";
        return cur + " " + price;
    }

    public void setOfferId(String offerId) { this.offerId = offerId; }
    public void setAirline(String airline) { this.airline = airline; }
    public void setAirlineName(String airlineName) { this.airlineName = airlineName; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public void setFromCity(String fromCity) { this.fromCity = fromCity; }
    public void setFromCode(String fromCode) { this.fromCode = fromCode; }
    public void setToCity(String toCity) { this.toCity = toCity; }
    public void setToCode(String toCode) { this.toCode = toCode; }
    public void setDepartureDate(String departureDate) { this.departureDate = departureDate; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setStops(int stops) { this.stops = stops; }
    public void setPrice(String price) { this.price = price; }
    public void setCurrency(String currency) { this.currency = currency; }

    public FlightSearch toFlightSearch() {
        FlightSearch fs = new FlightSearch(fromCity, toCity, departureDate, getFormattedPrice());
        fs.setAirline(airline);
        fs.setAirlineName(airlineName);
        fs.setFlightNumber(flightNumber);
        fs.setDepartureTime(departureTime);
        fs.setArrivalTime(arrivalTime);
        fs.setDuration(duration);
        fs.setStops(stops);
        return fs;
    }
}
