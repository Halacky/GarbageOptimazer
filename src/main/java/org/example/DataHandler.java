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

    // Поля для создания объекта ридера Excel файлов
    private FileInputStream fis;
    private XSSFWorkbook workbook;


    /**
     * Метод предназначенный для инициализации объекта, необходимого для чтения файлов.
     *
     * @param path Параметр типа String. Хранит в себе путь до файла, с которым предстоит работать
     * @return Возвращает объект типа XSSFSheet, который хранит в себе первый лист Excel.
     * @throws IOException Ошибки возникшие при чтении файла
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
     *
     * @param path Хранит в себе путь до файла, с которым предстоит работать. String
     * @return Map - key: Адрес, value: Координаты
     * @throws IOException Ошибки возникшие при чтении файла
     */
    protected Map<String, Coordinates<Double, Double>> getCoordinates(String path) throws IOException {
        // Получаем первый лист Excel файла
        XSSFSheet sheet = createExcelHandler(path);
        // Создаем словарь типа <key: Адрес, value: Координаты>
        Map<String, Coordinates<Double, Double>> objectFrom = new HashMap<>();
        // iterate on rows
        for (Row row : sheet) {
            // iterate on cells for the current row
            Iterator<Cell> cellIterator = row.cellIterator();
            Coordinates<Double, Double> coord;
            String rowStr = "";
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                rowStr += cell.toString() + "~";
            }

            int indexLat = 0;
            int indexLon = 0;
            int indexAddress = 0;

            if (Objects.equals(path, Storage.Garage)) {
                indexLat = 1;
                indexLon = 2;
            } else if (Objects.equals(path, Storage.Containers)) {
                indexLat = 10;
                indexLon = 11;
                indexAddress = 3;
            }
            double lat = Double.parseDouble(rowStr.split("~")[indexLat]);
            double lon = Double.parseDouble(rowStr.split("~")[indexLon]);
            coord = new Coordinates<>(lat, lon);
            objectFrom.put(rowStr.split("~")[indexAddress], coord);
        }

        workbook.close();
        fis.close();

        return objectFrom;
    }

    protected List<Garage> getGaragesInfo() throws IOException {
        // Получаем первый лист Excel файла
        XSSFSheet sheet = createExcelHandler(Storage.Cars);
        List<Garage> garages = new ArrayList<>();

        for (Row row : sheet) {
            // iterate on cells for the current row
            Iterator<Cell> cellIterator = row.cellIterator();
            Coordinates<Double, Double> coord;
            String rowStr = "";
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                rowStr += cell.toString() + "~";
            }


        }
        return null;
    }
}
