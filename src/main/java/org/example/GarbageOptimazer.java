package org.example;

import com.grum.geocalc.Coordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class GarbageOptimazer {

    /**
     * Метод предназначенный для подсчета метрик М1, М2, М3.
     * @return Array Double - массив метрик.
     */
    private double calculateMMetrix(double q, double s, double v){
        double M = q / s*v;
        return M;
    }

    /**
     * Метод предназначенный для создания матрицы расстояний.
     * @exception IOException Ошибки возникшие при чтении файла
     * @return List < List < Double >> - матрица расстояний.
     */
    public List<List<Double>> createDistanceMatrix() throws IOException {
        List<Garage> garagesInfo = new DataHandler().getGarage();
        List<Containers> containersInfo = new DataHandler().getContainers();
        List<List<Double>> distanceMatrix = new ArrayList<>();
        for (Garage garage : garagesInfo) {
            List<Double> row = new ArrayList<>();
            Coordinates<Double,Double> list = garage.getCoordinates();
            Coordinate lat = Coordinate.fromDegrees(list.getLatitude());
            Coordinate lng = Coordinate.fromDegrees(list.getLatitude());
            Point objFrom = Point.at(lat, lng);
            for (Containers container : containersInfo) {
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

    protected double calculateDistance(Point objFrom, Point objTo){
        return EarthCalc.haversine.distance(objFrom, objTo);
    }

}
