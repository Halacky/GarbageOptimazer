package org.example;

import java.util.List;

public class Garage {
    private double Id;
    private String Address;
    private List<Car> Cars;
    private Coordinates<Double,Double> Coordinates;

    public Garage(double id, String address, Coordinates<Double,Double> coordinates){
        Id = id;
        Address = address;
        Coordinates = coordinates;
        Cars = null;
    }

    public Coordinates<Double, Double> getCoordinates() {
        return Coordinates;
    }
}
