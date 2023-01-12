package org.example;

import com.grum.geocalc.Coordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;
import jdk.jshell.spi.SPIResolutionException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class GarbageOptimazer {

    private List<Garage> Garages;
    private List<Containers> Containers;

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
        Containers = new DataHandler().getContainers();
        List<List<Double>> distanceMatrix = new ArrayList<>();
        for (Garage garage : Garages) {
            List<Double> row = new ArrayList<>();
            Coordinates<Double,Double> list = garage.getCoordinates();
            Coordinate lat = Coordinate.fromDegrees(list.getLatitude());
            Coordinate lng = Coordinate.fromDegrees(list.getLatitude());
            Point objFrom = Point.at(lat, lng);
            for (Containers container : Containers) {
                list = container.getCoordinates();
                lat = Coordinate.fromDegrees(list.getLatitude());
                lng = Coordinate.fromDegrees(list.getLatitude());
                Point objTo = Point.at(lat, lng);
                double distance =  calculateDistance(objFrom,objTo);//in meters
                row.add(distance);
            }
            distanceMatrix.add(row);
        }
        System.out.println(distanceMatrix.get(0));
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

}
