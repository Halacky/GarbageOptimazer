package org.example;

/**
 * <h1>Класс сущности "Контейнерная площадка"</h1>
 * Данный класс содержит поля и методы, позволяющие хранить и изменять данные, характеризующие каждую контейнерную площадку.
 * @author  Головань Кирилл
 * @version 1.1
 * @since   2023-01-10
 */
public class Container implements Cloneable {
    private String Address; // Адрес КП
    private String Code;
    private Coordinates<Double, Double> Coordinates; // Коордианты КП
    private double TypeOfGrap; // Тип захвата
    private double Volume; // Объем КП
    private int Count; // Кол-во КП
    private double AllVolume; // Суммарный объем КП
    private boolean IsCater; // КП обслужена или нет
    private String CarNumber; // Номер обслуживающей машины
    private byte[] Schedule; // График вывоза

    /**
     * @param address Адрес КП
     * @param coordinates Коордианты КП
     * @param volume Объем КП
     * @param count Кол-во КП
     */
    public Container(String code, String address, Coordinates<Double, Double> coordinates, double volume, int count,byte[] schedule, double grubType){
        Address = address;
        Coordinates = coordinates;
        Volume = volume;
        Count = count;
        AllVolume = Volume * Count;
        IsCater = false;
        CarNumber = null;
        Schedule = schedule;
        TypeOfGrap = grubType;
        Code = code;
    }
    public Coordinates<Double, Double> getCoordinates() {
        return Coordinates;
    }

    public void setIsCater(boolean flag){
        IsCater = flag;
    }

    public void setCarNumber(String carNumber) {
        CarNumber = carNumber;
    }

    public double getAllVolume() {
        return AllVolume;
    }

    public String getAddress() {
        return Address;
    }

    public byte[] getSchedule() {
        return Schedule;
    }

    public boolean getIsCater() {
        return IsCater;
    }

    public double getTypeOfGrab() {
        return TypeOfGrap;
    }

    public String getCarNumber() {
        return CarNumber;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getCode() {
        return Code;
    }

    public double getVolume() {
        return Volume;
    }
}
