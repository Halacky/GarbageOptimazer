package org.example;

/**
 * <h1>Класс сущности "Контейнерная площадка"</h1>
 * Данный класс содержит поля и методы, позволяющие хранить и изменять данные, характеризующие каждую контейнерную площадку.
 * @author  Головань Кирилл
 * @version 1.1
 * @since   2023-01-10
 */
public class Containers {
    private String Address; // Адрес КП
    private Coordinates<Double, Double> Coordinates; // Коордианты КП
    private double TypeOfGrap;
    private double Volume; // Объем КП
    private int Count; // Кол-во КП
    private double AllVolume; // Суммарный объем КП
    private boolean IsCater; // КП обслужена или нет

    /**
     * @param address Адрес КП
     * @param coordinates Коордианты КП
     * @param volume Объем КП
     * @param count Кол-во КП
     */
    public Containers(String address, Coordinates<Double, Double> coordinates, double volume,int count){
        Address = address;
        Coordinates = coordinates;
        Volume = volume;
        Count = count;
        AllVolume = Volume * Count;
        IsCater = false;
    }
    public Coordinates<Double, Double> getCoordinates() {
        return Coordinates;
    }
}
