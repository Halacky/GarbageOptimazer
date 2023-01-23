package org.example;

import com.grum.geocalc.Coordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;

import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * <h1>Основной класс алгоритма</h1>
 * Данный класс содержит методы описывающие логику алгоритма.
 * @author  Головань Кирилл
 * @version 1.1
 * @since   2023-01-23
 */
public class GarbageOptimazer {
    private List<Garage> Garages; // список гаражей
    private List<Container> _Containers; // Список контейнеров
    private List<List<Double>> DistanceMatrix; // Матрица расстояний
    private Coordinates<Double,Double> Fcy; // Координаты самого удаленного объекта
    private int IndexOfFcy; // Индекс самого удаленного объекта

    /**
     * Метод предназначенный для подсчета метрик М1, М2, М3.
     * @param car - объект мышины, для которой необходимо посчитать метрики
     * @param v - расстояние, которое необходимо проехать машине
     * @return Double[] - массив метрик.
     */
    private double[] calculateMMetrix(Car car, double v){
        double M1 = car.getFuelConsum();
        double M2 = M1 / car.getCapacity();
        double M3 = M2*1/v;
        double[] MMetrix = new double[3];
        MMetrix[0] = M1;
        MMetrix[1] = M2;
        MMetrix[2] = M3;
        car.setM3(M3);
        return MMetrix;
    }

    /**
     * Метод предназначенный для создания матрицы расстояний.
     * @exception IOException Ошибки возникшие при чтении файла
     * @return List < List < Double >> - матрица расстояний.
     */
    public List<List<Double>> createDistanceMatrix() throws IOException {
        Garages = new DataHandler().fillGarage();
        _Containers = new DataHandler().getContainers();
        List<List<Double>> distanceMatrix = new ArrayList<>();
        for (Garage garage : Garages) {
            List<Double> row = new ArrayList<>();
            Coordinates<Double,Double> list = garage.getCoordinates();
            Coordinate lat = Coordinate.fromDegrees(list.getLatitude());
            Coordinate lng = Coordinate.fromDegrees(list.getLongitude());
            Point objFrom = Point.at(lat, lng);
            for (Container container : _Containers) {
                list = container.getCoordinates();
                lat = Coordinate.fromDegrees(list.getLatitude());
                lng = Coordinate.fromDegrees(list.getLongitude());
                Point objTo = Point.at(lat, lng);
                double distance =  calculateDistance(objFrom,objTo);//in meters
                row.add(distance);
            }
            distanceMatrix.add(row);
        }
        DistanceMatrix = distanceMatrix;

        findFcy();

        return distanceMatrix;

    }
    /**
     * Метод предназначенный для рассчета расстония между двумя точками.
     * @param objFrom Исходная точка
     * @param objTo Конечная точка
     * @return  Double - расстояниме между точками.
     */
    protected double calculateDistance(Point objFrom, Point objTo){
        return EarthCalc.haversine.distance(objFrom, objTo);
    }

    /**
     * Метод предназначенный для рассчета сумм расстояний всеми точками отправлений и всеми точками назначений.
     * @return  List Double - суммы расстояний.
     */
    private List<Double> sumDistance(){
        int size = DistanceMatrix.get(0).size();
        List<Double> sumDistance = new ArrayList<>(Collections.nCopies(size, 0.0));
        System.out.println(sumDistance.size());
        for (List<Double> distanceMatrix : DistanceMatrix) {
            for (int i = 0; i < distanceMatrix.size(); i++) {
                sumDistance.set(i,sumDistance.get(i)+distanceMatrix.get(i));
            }
        }
        return sumDistance;
    }

    /**
     * Метод предназначенный для нахождения саммой удаленной точки назначения.
     */
    private void findFcy(){
        IndexOfFcy = getIndexOfLargest(sumDistance());
        Fcy = _Containers.get(IndexOfFcy).getCoordinates();

        List<Garage> optimalGarages = getOptimalGarages(IndexOfFcy);
        Car bestCar = getBestCar(optimalGarages);
    }

    /**
     * Метод предназначенный для добавления строки в матрицу расстояний.
     * @param objF Исходная точка
     */
    private void addRowInMatrix(Container objF){
            List<Double> row = new ArrayList<>();
            Coordinates<Double, Double> list = objF.getCoordinates();
            Coordinate lat = Coordinate.fromDegrees(list.getLatitude());
            Coordinate lng = Coordinate.fromDegrees(list.getLongitude());
            Point objFrom = Point.at(lat, lng);
            for (Container container : _Containers) {
                list = container.getCoordinates();
                lat = Coordinate.fromDegrees(list.getLatitude());
                lng = Coordinate.fromDegrees(list.getLongitude());
                Point objTo = Point.at(lat, lng);
                double distance = calculateDistance(objFrom, objTo);//in meters
                row.add(distance);
            }
            DistanceMatrix.add(row);

            System.out.println(DistanceMatrix.size());
    }

    /**
     * Метод предназначенный для нахождения лучшей машины среди выбранных гаражей.
     * @param optimalGarages Список выбранных (по удаленности) гаражей
     * @return  Car - объект лучшей машины.
     */
    private Car getBestCar(List<Garage> optimalGarages){
        double tmpMinM = Double.MAX_VALUE;
        Car bestCar = null;
        for (Garage garage : optimalGarages){
            for (Car car: garage.getCars()){
                calculateMMetrix(car, DistanceMatrix.get((int)(garage.getId()-1)).get(IndexOfFcy));
                if (car.getM3() < tmpMinM){
                    bestCar = car;
                }
            }
        }
        System.out.println();
        servicingContainer(bestCar);
        return bestCar;
    }

    private void getNeighbors(Car car){
        // TODO проверка вместимости
        Container lastCont = car.getServicesContainers().get(car.getServicesContainers().size()-1);
        addRowInMatrix(lastCont);
        int nearest = getIndexOfSmallest(sumDistance());
        System.out.println(nearest);
    }

    private void servicingContainer(Car car){
        Container container = _Containers.get(IndexOfFcy);
        List<Container> serviceCont = new ArrayList<>();
        container.setCarNumber(car.getNumber());
        container.setIsCater(true);
        Coordinates<Double, Double> centroid = new Coordinates<>(
                container.getCoordinates().getLongitude(),
                container.getCoordinates().getLatitude()
        );
        car.setCentroid(centroid);
        serviceCont.add(container);
        car.setServicesContainers(serviceCont);
        getNeighbors(car);
    }
    /**
     * Метод предназначенный для нахождения лучших гаражей. Выбор происходит на основе сумм расстояний
     * @param fcy Индекс саммой удаленной точки, до неё будет искать суммы расстояний
     * @return  List Garage - объекты самых ближайщих гаражей.
     */
    private List<Garage> getOptimalGarages(int fcy){
        List<Double> optimalGarageIndexes = new ArrayList<>();
        List<Garage> optimalGarage = new ArrayList<>();
        for (int i = 0; i<3; i++){
            double tmpMin = Double.MAX_VALUE;
            double g = 0.0;
            double gMin = 0.0;
            for (List<Double> distanceMatrix : DistanceMatrix) {
                double current = distanceMatrix.get(fcy);
                if (current < tmpMin & !(optimalGarageIndexes.contains(g))){
                    tmpMin = distanceMatrix.get(fcy);
                    gMin = g;
                }
                g++;
            }
            optimalGarageIndexes.add(gMin);
            optimalGarage.add(Garages.get((int) gMin));
        }
        return optimalGarage;
    }

    /**
     * Метод предназначенный для нахождения инедкса наибольшего значения в массиве.
     * @param array Массив чисел
     * @return  int - индекс наибольшего числа
     */
    private int getIndexOfLargest(List<Double> array )
    {
        if ( array == null || array.size() == 0 ) return -1;

        int largest = 0;
        for ( int i = 1; i < array.size(); i++ )
        {
            if ( array.get(i) > array.get(largest)) largest = i;
        }
        return largest;
    }

    /**
     * Метод предназначенный для нахождения инедкса наименьшего значения в массиве.
     * @param array Массив чисел
     * @return  int - индекс наименьшего числа
     */
    private int getIndexOfSmallest(List<Double> array)
    {
        if ( array == null || array.size() == 0 ) return -1;

        int smallest = 0;
        for ( int i = 1; i < array.size(); i++ )
        {
            if ( array.get(i) < array.get(smallest) & array.get(i) > 0) smallest = i;
        }
        return smallest;
    }

}
