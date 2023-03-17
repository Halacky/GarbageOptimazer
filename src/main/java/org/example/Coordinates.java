package org.example;

public class Coordinates<Lat,Lon> {

    private final Lon longitude;
    private final Lat latitude;

    public Coordinates(Lat latitude,Lon longitude) {
        assert longitude != null;
        assert latitude != null;

        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Lon getLongitude() { return longitude; }
    public Lat getLatitude() { return latitude; }

    @Override
    public int hashCode() { return longitude.hashCode() ^ latitude.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Coordinates)) return false;
        Coordinates pairo = (Coordinates) o;
        return this.longitude.equals(pairo.getLongitude()) &&
                this.latitude.equals(pairo.getLatitude());
    }

}