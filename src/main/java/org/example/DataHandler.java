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
     * @return Map - key: Адрес, value: Координаты
     * @throws IOException Ошибки возникшие при чтении файла
     */
    protected List<Garage> getGarage() throws IOException {
        // Получаем первый лист Excel файла
        XSSFSheet sheet = createExcelHandler( Storage.Garage);
        List<Garage> garages = new ArrayList<>();
        String rowStr = "";
        for (Row row : sheet) {
            rowStr = iterateRow(row);
            int indexLat = 2;
            int indexLon = 3;
            int indexAddress = 1;
            int indexGarageID = 0;

            double lat = Double.parseDouble(rowStr.split("~")[indexLat]);
            double lon = Double.parseDouble(rowStr.split("~")[indexLon]);
            double id = Double.parseDouble(rowStr.split("~")[indexGarageID]);
            String address = rowStr.split("~")[indexAddress];
            Coordinates<Double, Double> coord = new Coordinates<>(lat, lon);
            garages.add(new Garage(id, address, coord));
        }
        workbook.close();
        fis.close();

        return garages;
    }

    protected List<Containers> getContainers() throws IOException {
        // Получаем первый лист Excel файла
        XSSFSheet sheet = createExcelHandler(Storage.Containers);
        List<Containers> containers = new ArrayList<>();
        String rowStr = "";
        for (Row row : sheet) {
            rowStr = iterateRow(row);
            int indexLat = 10;
            int indexLon = 11;
            int indexAddress = 3;
            int indexCount = 12;
            int indexVolume = 13;

            double lat = Double.parseDouble(rowStr.split("~")[indexLat]);
            double lon = Double.parseDouble(rowStr.split("~")[indexLon]);
            int count = Integer.parseInt(rowStr.split("~")[indexCount]);
            double volume = Double.parseDouble(rowStr.split("~")[indexVolume]);
            String address = rowStr.split("~")[indexAddress];
            Coordinates<Double, Double> coord = new Coordinates<>(lat, lon);
            containers.add(new Containers(address, coord,volume,count));
        }
        workbook.close();
        fis.close();

        return containers;
    }

    private String iterateRow(Row row){
        // iterate on cells for the current row
        Iterator<Cell> cellIterator = row.cellIterator();
        String rowStr = "";
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            rowStr += cell.toString() + "~";
        }
        return rowStr;
    }
}
