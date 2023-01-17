package org.example;

import com.grum.geocalc.Coordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;
import jdk.jshell.spi.SPIResolutionException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;


public class GarbageOptimazer {

    private List<Garage> Garages;
    private List<Containers> _Containers;
    private List<List<Double>> DistanceMatrix;
    private Coordinates<Double,Double> Fcy;

    /**
     * Метод предназначенный для подсчета метрик М1, М2, М3.
     * @param q Расход топлива на 1 км
     * @param s Расстояние, которое необходимо пройти
     * @param v Свободный объем кузова
     * @return Double[] - массив метрик.
     */
    private double[] calculateMMetrix(double q, double s, double v){
        double M1 = q;
        double M2 = M1 / v;
        double M3 = q / s*v;
        double[] MMetrix = new double[3];
        MMetrix[0] = M1;
        MMetrix[1] = M2;
        MMetrix[2] = M3;
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
            Coordinate lng = Coordinate.fromDegrees(list.getLatitude());
            Point objFrom = Point.at(lat, lng);
            for (Containers container : _Containers) {
                list = container.getCoordinates();
                lat = Coordinate.fromDegrees(list.getLatitude());
                lng = Coordinate.fromDegrees(list.getLatitude());
                Point objTo = Point.at(lat, lng);
                double distance =  calculateDistance(objFrom,objTo);//in meters
                row.add(distance);
            }
            distanceMatrix.add(row);
        }
        DistanceMatrix = distanceMatrix;

        Fcy = findFcy();
        System.out.println(Fcy);

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

    private Coordinates<Double,Double> findFcy(){
        int size = DistanceMatrix.get(0).size();
        List<Double> sumDistance = new ArrayList<>(Collections.nCopies(size, 0.0));
        System.out.println(sumDistance.size());
        for (List<Double> distanceMatrix : DistanceMatrix) {
            for (int i = 0; i < distanceMatrix.size(); i++) {
                sumDistance.set(i,sumDistance.get(i)+distanceMatrix.get(i));
            }
        }
        int maxDistance = getIndexOfLargest(sumDistance);
//        System.out.println(sumDistance);

        List<Garage> optimalGarages = findOptimalGarages(maxDistance);
        System.out.println(optimalGarages);
        return _Containers.get(maxDistance).getCoordinates();
    }

    private List<Garage> findOptimalGarages(int fcy){
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

}
