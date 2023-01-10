package org.example;

import java.io.IOException;

/**
 * <h1>Класс сущности "машина"</h1>
 * Данный класс содержит поля и методы, позволяющие хранить и изменять данные, характеризующие каждую машину.
 * @author  Головань Кирилл
 * @version 1.1
 * @since   2023-01-10
 */
public class Car {
    private String Number; // Гос. номер
    private double M1; // М1 - метрика холостого хода
    private double M2; // М2 - метрика удельного расхода топлива
    private double M3; // М3 - модифицированная метрика М2
    private double Centroid; // Центроид обрабатываемого класстера
    private double Capacity; // Грузоподъемность
    private double TypeOfGrab; // Тип пагрузки 1-Боковой, 2-Задний, 3-Бункеры, 4-универсальные
    private Coordinates<Double, Double> Coordinates; // Местоположение машины
    private double GarageId; // Идентификатор гаража
    private String FuelType; // Тип топлива
    private String WorkSchedule; // График работы

    /**
     * @param number Гос. номер
     * @param capacity Грузоподъемность
     * @param typeOfGrab Тип пагрузки 1-Боковой, 2-Задний, 3-Бункеры, 4-универсальные
     * @param garageId Идентификатор гаража
     * @param fuelType Тип топлива
     * @param workSchedule График работы
     */
    public Car(String number, double capacity,double typeOfGrab,double garageId, String fuelType,String workSchedule){
        Centroid = 0;
        Number = number;
        Capacity = capacity;
        TypeOfGrab = typeOfGrab;
        GarageId = garageId;
        FuelType = fuelType;
        WorkSchedule = workSchedule;
    }

    public double getCentroid() {
        return Centroid;
    }

    public double getGarageId() {
        return GarageId;
    }

    public void setCentroid(double centroid) {
        Centroid = centroid;
    }

    public void setCoordinates(Coordinates<Double, Double> coordinates) {
        Coordinates = coordinates;
    }

    public void setCapacity(double capacity) {
        Capacity = capacity;
    }
}
