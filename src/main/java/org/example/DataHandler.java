package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import com.grum.geocalc.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
public class DataHandler {
    private static final String Garage  = "C:\\Users\\gR9r\\Desktop\\Мусоровозы\\Data\\Garages.xlsx";
    private static final String Polygons  = "C:\\Users\\gR9r\\Desktop\\Мусоровозы\\Data\\Polygons.xlsx";
    private static final String Containers  = "C:\\Users\\gR9r\\Desktop\\Мусоровозы\\Data\\Containers.xlsx";
    private static final String Cars  = "C:\\Users\\gR9r\\Desktop\\Мусоровозы\\Data\\Cars.xlsx";
    private File excelFile;
    private FileInputStream fis;
    private XSSFWorkbook workbook;
    private XSSFSheet createExcelHandler(String path) throws IOException {
        excelFile = new File(path);
        fis = new FileInputStream(excelFile);

        // we create an XSSF Workbook object for our XLSX Excel File
        workbook = new XSSFWorkbook(fis);
        // we get first sheet
        return workbook.getSheetAt(0);
    }
    private Map<String, List<Double>> getGaragesCoordinates() throws IOException {

        XSSFSheet sheet = createExcelHandler(Garage);
        Map<String, List<Double>> objectFrom = new HashMap<String, List<Double>>();
        // iterate on rows
        for (Row row : sheet) {
            // iterate on cells for the current row
            Iterator<Cell> cellIterator = row.cellIterator();
            List<Double> coord = new ArrayList<>();
            String rowStr = "";
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                rowStr += cell.toString() + "~";
            }

//            System.out.println(rowStr);
            coord.add(Double.parseDouble(rowStr.split("~")[1]));
            coord.add(Double.parseDouble(rowStr.split("~")[2]));
            objectFrom.put(rowStr.split("~")[0],coord);
//            System.out.println();
        }

        workbook.close();
        fis.close();

        return objectFrom;
    }

    private Map<String, List<Double>> getContainersCoordinates() throws IOException {

        XSSFSheet sheet = createExcelHandler(Containers);
        Map<String, List<Double>> objectFrom = new HashMap<String, List<Double>>();
        // iterate on rows
        for (Row row : sheet) {
            // iterate on cells for the current row
            Iterator<Cell> cellIterator = row.cellIterator();
            List<Double> coord = new ArrayList<>();
            String rowStr = "";
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                rowStr += cell.toString() + "~";
            }
//            System.out.println(rowStr);
            coord.add(Double.parseDouble(rowStr.split("~")[10]));
            coord.add(Double.parseDouble(rowStr.split("~")[11]));
            objectFrom.put(rowStr.split("~")[3],coord);
//            System.out.println();
        }

        workbook.close();
        fis.close();

        return objectFrom;
    }

    public List<List<Double>> createDistanceMatrix() throws IOException {
        Map<String, List<Double>> garagesInfo = new DataHandler().getGaragesCoordinates();
        Map<String, List<Double>> containersInfo = new DataHandler().getContainersCoordinates();
        List<List<Double>> distanceMatrix = new ArrayList<>();
        for (Map.Entry<String, List<Double>> garage : garagesInfo.entrySet()) {
            List<Double> row = new ArrayList<>();
            List<Double> list = garage.getValue();
            Coordinate lat = Coordinate.fromDegrees(list.get(0));
            Coordinate lng = Coordinate.fromDegrees(list.get(1));
            Point objFrom = Point.at(lat, lng);
            for (Map.Entry<String, List<Double>> container : containersInfo.entrySet()) {
                list = container.getValue();
                lat = Coordinate.fromDegrees(list.get(0));
                lng = Coordinate.fromDegrees(list.get(1));
                Point objTo = Point.at(lat, lng);
                double distance = EarthCalc.haversine.distance(objFrom, objTo); //in meters
                row.add(distance);
            }
            distanceMatrix.add(row);
        }
        System.out.println(distanceMatrix.get(0));
        return distanceMatrix;
    }

}
