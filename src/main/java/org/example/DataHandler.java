package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import com.grum.geocalc.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
/**
 * <h1>Класс для работы с данными</h1>
 * Данный класс содержит поля и методы, позволяющие обрабатывать входные данные.
 * На текущий момент возможно считывать данные из Excel, строго определенной структуры.
 * Описание необходимой структуры описывается в каждом методе отдельно.
 * @author  Головань Кирилл
 * @version 1.0
 * @since   2022-12-25
 */
public class DataHandler {
    // Статические поля, хранящие в себе абсолютные ссылки, на локально расположенные файлы с данными
    private static final String Garage  = "C:\\Users\\gR9r\\Desktop\\Мусоровозы\\Data\\Garages.xlsx";  // Файл с информацией о гаражах
    private static final String Polygons  = "C:\\Users\\gR9r\\Desktop\\Мусоровозы\\Data\\Polygons.xlsx"; // Файл с информацией о полигонах
    private static final String Containers  = "C:\\Users\\gR9r\\Desktop\\Мусоровозы\\Data\\Containers.xlsx"; // Файл с информацией о контейнерах
    private static final String Cars  = "C:\\Users\\gR9r\\Desktop\\Мусоровозы\\Data\\Cars.xlsx"; // Файл с информацией о машинах


    // Поля для создания объекта ридера Excel файлов
    private FileInputStream fis;
    private XSSFWorkbook workbook;


    /**
     * Метод предназначенный для инициализации объекта, необходимого для чтения файлов.
     * @param path Параметр типа String. Хранит в себе путь до файла, с которым предстоит работать
     * @exception IOException Ошибки возникшие при чтении файла
     * @return Возвращает объект типа XSSFSheet, который хранит в себе первый лист Excel.
     */
    private XSSFSheet createExcelHandler(String path) throws IOException {
        // Получение доступа к файлу, указанному в пути
        File excelFile = new File(path);
        // Создание потока
        fis = new FileInputStream(excelFile);
        // Создание XSSF Workbook объекта для чтения Excel файла
        workbook = new XSSFWorkbook(fis);
        // Возвращаем первый лист Excel Файла
        return workbook.getSheetAt(0);
    }

    /**
     * Метод предназначенный для получения координат всех объектов.
     * Требования к обрабатываемому документу:
     * <ol>
     *     <li>Остутствие заглавия документа (чтение начинается с ячейки A1)</li>
     *     <li>Столбец с индексом indexAddress - адрес гаража</li>
     *     <li>Столбец с индексом indexLat - широта гаража</li>
     *     <li>Столбец с индексом indexLon - долгота гаража</li>
     * </ol>
     * @param path Хранит в себе путь до файла, с которым предстоит работать. String
     * @exception IOException Ошибки возникшие при чтении файла
     * @return Map < String, Coordinates< Double, Double > > - <Адрес гаража, Координаты гаража>.
     */
    private Map<String, Coordinates<Double, Double>> getGaragesCoordinates(String path) throws IOException {

        XSSFSheet sheet = createExcelHandler(path);
        Map<String, Coordinates<Double, Double>> objectFrom = new HashMap<>();
        // iterate on rows
        for (Row row : sheet) {
            // iterate on cells for the current row
            Iterator<Cell> cellIterator = row.cellIterator();
            Coordinates<Double,Double> coord;
            String rowStr = "";
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                rowStr += cell.toString() + "~";
            }

            int indexLat = 0;
            int indexLon = 0;
            int indexAddress = 0;

            if (Objects.equals(path, Garage)){
                indexLat = 1;
                indexLon = 2;
            } else if (Objects.equals(path, Containers)) {
                indexLat = 10;
                indexLon = 11;
                indexAddress = 3;
            }
            double lat = Double.parseDouble(rowStr.split("~")[indexLat]);
            double lon = Double.parseDouble(rowStr.split("~")[indexLon]);
            coord = new Coordinates<>(lat,lon);
            objectFrom.put(rowStr.split("~")[indexAddress],coord);
        }

        workbook.close();
        fis.close();

        return objectFrom;
    }

    /**
     * Метод предназначенный для создания матрицы расстояний.
     * @exception IOException Ошибки возникшие при чтении файла
     * @return List < List < Double >> - матрица расстояний.
     */
    public List<List<Double>> createDistanceMatrix() throws IOException {
        Map<String, Coordinates<Double,Double>> garagesInfo = new DataHandler().getGaragesCoordinates(Garage);
        Map<String, Coordinates<Double,Double>> containersInfo = new DataHandler().getGaragesCoordinates(Containers);
        List<List<Double>> distanceMatrix = new ArrayList<>();
        for (Map.Entry<String,Coordinates<Double,Double>> garage : garagesInfo.entrySet()) {
            List<Double> row = new ArrayList<>();
            Coordinates<Double,Double> list = garage.getValue();
            Coordinate lat = Coordinate.fromDegrees(list.getLatitude());
            Coordinate lng = Coordinate.fromDegrees(list.getLatitude());
            Point objFrom = Point.at(lat, lng);
            for (Map.Entry<String,Coordinates<Double,Double>> container : containersInfo.entrySet()) {
                list = container.getValue();
                lat = Coordinate.fromDegrees(list.getLatitude());
                lng = Coordinate.fromDegrees(list.getLatitude());
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
