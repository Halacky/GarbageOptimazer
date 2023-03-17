package org.example;

public class Polygon {
    private int PolygonId;
    private String Address;
    private Coordinates<Double, Double> Coordinates;

    Polygon(int id, String address, Coordinates<Double,Double> coordinates){
        PolygonId = id;
        Address = address;
        Coordinates = coordinates;
    }

    public org.example.Coordinates<Double, Double> getCoordinates() {
        return Coordinates;
    }

    public String getAddress() {
        return Address;
    }
}
