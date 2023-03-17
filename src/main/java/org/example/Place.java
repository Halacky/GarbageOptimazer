package org.example;

import java.util.List;
import java.util.function.Predicate;

/**
 * <h1>Класс сущности "гараж"</h1>
 * Данный класс содержит поля и методы, позволяющие хранить и изменять данные, характеризующие каждый гараж.
 * @author  Головань Кирилл
 * @version 1.1
 * @since   2023-01-10
 */
public class Place implements Comparable<Place>{
    private double Id; // Идентификатор гаража
    private String Address; // Адрес гаража
    private List<Car> Cars; // Список машин, находящихся в гараже
    private Coordinates<Double,Double> Coordinates; // Координаты гаража
    private Double DistanceToFcy;
    private double GarageIndex;

    /**
     * @param id Идентификатор гаража
     * @param address Адрес гаража
     * @param coordinates Координаты гаража
     */
    public Place(double id, String address, Coordinates<Double,Double> coordinates, double index){
        Id = id;
        Address = address;
        Coordinates = coordinates;
        Cars = null;
        GarageIndex = index;
    }

    public Coordinates<Double, Double> getCoordinates() {
        return Coordinates;
    }

    public double getId() {
        return Id;
    }

    public List<Car> getCars() {
        return Cars;
    }

    public void setCars(List<Car> cars) {
        Cars = cars;
    }

    public String getAddress() {
        return Address;
    }

    public Double getDistanceToFcy() {
        return DistanceToFcy;
    }

    public void setDistanceToFcy(Double distanceToFcy) {
        DistanceToFcy = distanceToFcy;
    }

    @Override
    public int compareTo(Place o) {
        if (getDistanceToFcy() == null || o.getDistanceToFcy() == null) {
            return 0;
        }
        return getDistanceToFcy().compareTo(o.getDistanceToFcy());
    }

    public double getGarageIndex() {
        return GarageIndex;
    }

    public static Place getPlaceById(int pid, List<Place> list) {
        Place place = list.stream()
                .filter(findEmp -> pid == findEmp.getId())
                .findAny().orElse(null);
        return place;
    }
}
