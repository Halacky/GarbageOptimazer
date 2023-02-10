package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private Coordinates<Double, Double> Centroid; // Центроид обрабатываемого класстера
    private double Capacity; // Грузоподъемность
    private double Volume; // Объем кузова
    private double CompactionRatio; // Коэффициенет уплотнения
    private double TypeOfGrab; // Тип пагрузки 1-Боковой, 2-Задний, 3-Бункеры, 4-универсальные
    private Coordinates<Double, Double> Coordinates; // Местоположение машины
    private double GarageId; // Идентификатор гаража
    private String FuelType; // Тип топлива
    private String WorkSchedule; // График работы
    private double FuelConsum; // Расход топлива на 1 км
    private List<Container> ServicesContainers; // Обслуженные машины
    private boolean InWork; // Индикатор, отображающий, находится ли машина в работе или нет
    private double TimeInWork; // Время в работе

    /**
     * @param number Гос. номер
     * @param capacity Грузоподъемность
     * @param typeOfGrab Тип пагрузки 1-Боковой, 2-Задний, 3-Бункеры, 4-универсальные
     * @param garageId Идентификатор гаража
     * @param fuelType Тип топлива
     * @param workSchedule График работы
     */
    public Car(String number, double capacity,double typeOfGrab,double garageId, String fuelType,String workSchedule, double fuelConsum, double compactionRatio, double volume){
        Centroid = null;
        Number = number;
        Capacity = capacity;
        TypeOfGrab = typeOfGrab;
        GarageId = garageId;
        FuelType = fuelType;
        WorkSchedule = workSchedule;
        FuelConsum = fuelConsum;
        ServicesContainers = new ArrayList<>();
        CompactionRatio = compactionRatio;
        Volume = volume;
        InWork = false;
        TimeInWork = 0;
    }

    public Coordinates<Double, Double> getCentroid() {
        return Centroid;
    }
    public double getGarageId() {
        return GarageId;
    }
    public void setCentroid(Coordinates<Double, Double> centroid) {
        Centroid = centroid;
    }
    public void setCoordinates(Coordinates<Double, Double> coordinates) {
        Coordinates = coordinates;
    }
    public void setCapacity(double capacity) {
        Capacity = capacity;
    }
    public double getTypeOfGrab() {
        return TypeOfGrab;
    }
    public double getFuelConsum() {
        return FuelConsum;
    }
    public double getCapacity() {
        return Capacity;
    }
    public void setM3(double m3) {
        M3 = m3;
    }
    public double getM3() {
        return M3;
    }
    public String getNumber() {
        return Number;
    }
    public void setServicesContainers(Container container) {
        ServicesContainers.add(container);
    }
    public List<Container> getServicesContainers() {
        return ServicesContainers;
    }

    public void setInWork(boolean inWork) {
        InWork = inWork;
    }
    public boolean isInWork() {
        return InWork;
    }

    public void setTimeInWork(double timeInWork) {
        TimeInWork = timeInWork;
    }

    public double getTimeInWork() {
        return TimeInWork;
    }
}
