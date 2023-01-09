package org.example;

public class Containers {
    private String Address;
    private Coordinates<Double, Double> Coordinates;
    private double Volume;
    private int Count;
    private double AllVolume;
    public Containers(String address, Coordinates<Double, Double> coordinates, double volume,int count){
        Address = address;
        Coordinates = coordinates;
        Volume = volume;
        Count = count;
        AllVolume = Volume * Count;
    }
    public Coordinates<Double, Double> getCoordinates() {
        return Coordinates;
    }
}
