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
    private List<Container> AllServiceCont;
    private List<Double> parentCluster;

    public GarbageOptimazer() throws IOException {
        createDistanceMatrix();
        AllServiceCont = new ArrayList<>();
    }

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
    protected void findFcy(){
        while (AllServiceCont.size() <= _Containers.size()){
            System.out.println(AllServiceCont.size());
            if(AllServiceCont.size() == 237){
                System.out.println();
            }
            int indexOfFcy = getIndexOfLargest(sumDistance());
            Fcy = _Containers.get(indexOfFcy).getCoordinates();

            List<Garage> optimalGarages = getOptimalGarages(indexOfFcy);
            Car bestCar = getBestCar(optimalGarages,indexOfFcy);
            addRowInMatrix(_Containers.get(indexOfFcy));

            servicingContainer(bestCar,indexOfFcy);
            DistanceMatrix.remove(DistanceMatrix.size()-1);
        }

    }

    private void removeTemporaryOF(Car car, int index){
        for(int i = 1;i<=car.getServicesContainers().size();i++){
            DistanceMatrix.remove(DistanceMatrix.size()-i);
        }
    }

    private void removeTemporaryOT(int index){
        for (List<Double> list: DistanceMatrix){
            list.remove(index);
        }
        _Containers.remove(index);
    }

    private boolean checkCarCapacity(Car bestCar, int index){
        return bestCar.getCapacity() >= _Containers.get(index).getAllVolume();
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

    }

    /**
     * Метод предназначенный для нахождения лучшей машины среди выбранных гаражей.
     * @param optimalGarages Список выбранных (по удаленности) гаражей
     * @return  Car - объект лучшей машины.
     */
    private Car getBestCar(List<Garage> optimalGarages, int indexOfFcy){
        double tmpMinM = Double.MAX_VALUE;
        Car bestCar = null;
        for (Garage garage : optimalGarages){
            for (Car car: garage.getCars()){
                calculateMMetrix(car, DistanceMatrix.get((int)(garage.getId()-1)).get(indexOfFcy));
                if (checkCarCapacity(car, indexOfFcy) & !car.isInWork()){
                    if (car.getM3() < tmpMinM){
                        bestCar = car;
                    }
                }
            }
        }
        return bestCar;
    }

    private void getNeighbors(Car car){
        int indexNearest = findNearest(car);
        if(indexNearest==21846){
            System.out.println();
        }
        servicingContainer(car, indexNearest);
    }

    private int findNearest(Car car){
        List<Double> parent = DistanceMatrix.get(DistanceMatrix.size()-1);
        double max = Double.MIN_VALUE;
        int indexMax = 0;
        for(int i = 0; i<parent.size();i++){
            if(parent.get(i) != 0){
                double importanceContainer = _Containers.get(i).getAllVolume()/parent.get(i);
                if (importanceContainer>max){
                    max = importanceContainer;
                    indexMax = i;
                }
            }

        }
        return indexMax;
    }

    private void servicingContainer(Car car, int indexOfFcy){
        if(checkCarCapacity(car,indexOfFcy)){
            Container container = _Containers.get(indexOfFcy);
            container.setCarNumber(car.getNumber());
            container.setIsCater(true);
            Coordinates<Double, Double> centroid = new Coordinates<>(
                    container.getCoordinates().getLongitude(),
                    container.getCoordinates().getLatitude()
            );
            car.setCapacity(car.getCapacity()-container.getAllVolume());
            car.setCentroid(centroid);
            car.setServicesContainers(container);
            car.setInWork(true);
            AllServiceCont.add(container);
            removeTemporaryOT(indexOfFcy);
            getNeighbors(car);
        }

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
        if (array == null || array.size() == 0) return -1;

        int smallest = 0;
        for (int i = 1; i < array.size(); i++)
        {
            double currentMetric = _Containers.get(i).getAllVolume()/array.get(i);
            double maxMetric = _Containers.get(smallest).getAllVolume()/array.get(smallest);
            if (currentMetric>maxMetric & array.get(i) > 0) {
                smallest = i;
            }
        }
        return smallest;
    }

}
//TODO пересчитать центроиды
//TODO проверка близости центроида при выборе машины
