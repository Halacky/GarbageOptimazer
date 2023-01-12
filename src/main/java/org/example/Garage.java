package org.example;

import java.util.List;
import java.util.Map;

/**
 * <h1>Класс сущности "гараж"</h1>
 * Данный класс содержит поля и методы, позволяющие хранить и изменять данные, характеризующие каждый гараж.
 * @author  Головань Кирилл
 * @version 1.1
 * @since   2023-01-10
 */
public class Garage {
    private double Id; // Идентификатор гаража
    private String Address; // Адрес гаража
    private List<Car> Cars; // Список машин, находящихся в гараже
    private Coordinates<Double,Double> Coordinates; // Координаты гаража

    /**
     * @param id Идентификатор гаража
     * @param address Адрес гаража
     * @param coordinates Координаты гаража
     */
    public Garage(double id, String address, Coordinates<Double,Double> coordinates){
        Id = id;
        Address = address;
        Coordinates = coordinates;
        Cars = null;
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

}
