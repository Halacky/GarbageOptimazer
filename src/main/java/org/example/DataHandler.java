package org.example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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
     * Метод предназначенный для получения информации о всех гаражах.
     * Требования к обрабатываемому документу:
     * <ol>
     *     <li>Остутствие заглавия документа (чтение начинается с ячейки A1)</li>
     *     <li>Столбец с индексом indexAddress - адрес гаража</li>
     *     <li>Столбец с индексом indexLat - широта гаража</li>
     *     <li>Столбец с индексом indexLon - долгота гаража</li>
     *     <li>Столбец с индексом indexGarageID - Идентификатор гаража</li>
     * </ol>
     * @return Список объектов типа Place
     * @throws IOException Ошибки возникшие при чтении файла
     */
    private List<Place> getGarage() throws IOException {
        // Получаем первый лист Excel файла
        XSSFSheet sheet = createExcelHandler(Storage.Garage);
        List<Place> places = new ArrayList<>();
        String rowStr = "";
        int idd = 0;
        for (Row row : sheet) {
            rowStr = iterateRow(row);
            int indexLat = 2;
            int indexLon = 3;
            int indexAddress = 1;
            int indexGarageID = 0;

            double lat = Double.parseDouble(rowStr.split("~")[indexLat]);
            double lon = Double.parseDouble(rowStr.split("~")[indexLon]);
            double index = Double.parseDouble(rowStr.split("~")[indexGarageID]);
            String address = rowStr.split("~")[indexAddress];
            Coordinates<Double, Double> coord = new Coordinates<>(lat, lon);
            if(index == 6 || index == 5 || index == 0){
                places.add(new Place(idd, address, coord, index));
                idd++;
            }
        }
        workbook.close();
        fis.close();

        return places;
    }

    /**
     * Метод предназначенный для наполнения гаражей информацией об находящихся в них машинах.
     * @return Список объектов типа Place
     * @throws IOException Ошибки возникшие при чтении файла
     */
    public List<Place> fillGarage() throws IOException {
        List<Place> places = getGarage();
        List<Car> cars = getCars();

        for(Place place : places) {
            List<Car> tmpCars = new ArrayList<>();
            for (Car car : cars) {
                if (place.getGarageIndex()==car.getGarageId()){
                    car.setCoordinates(place.getCoordinates());
                    tmpCars.add(car);
                }
            }
            place.setCars(tmpCars);
        }

        return places;
    }

    /**
     * Метод предназначенный для получения информации о всех контейнерах.
     * Требования к обрабатываемому документу:
     * <ol>
     *     <li>Остутствие заглавия документа (чтение начинается с ячейки A1)</li>
     *     <li>Столбец с индексом indexAddress - адрес контейнера</li>
     *     <li>Столбец с индексом indexLat - широта контейнера</li>
     *     <li>Столбец с индексом indexLon - долгота контейнера</li>
     *     <li>Столбец с индексом indexCount - количество контейнеров</li>
     *     <li>Столбец с индексом indexVolume - объем контейнера</li>
     * </ol>
     * @return Список объектов типа Containers
     * @throws IOException Ошибки возникшие при чтении файла
     */
    protected List<Container> getContainers(int numberDay) throws IOException {
        // Получаем первый лист Excel файла
        XSSFSheet sheet = createExcelHandler(Storage.Containers);
        List<Container> containers = new ArrayList<>();
        String rowStr = "";
        Calendar nextM = getNextMonth(new Date());
        int countRows = 0;
        for (Row row : sheet) {
            if (countRows == 0) {
                countRows ++;
            }else{
                rowStr = iterateRow(row);
                int indexCityName = 0;
                int indexAddress = 3;
                int indexLat = 10;
                int indexLon = 11;
                int indexCount = 12;
                int indexVolume = 13;
                int indexSchedule = 14;
                int indexTypeOfGrub = 21;

                double lat = Double.parseDouble(rowStr.split("~")[indexLat]);
                double lon = Double.parseDouble(rowStr.split("~")[indexLon]);
                double count = Double.parseDouble(rowStr.split("~")[indexCount]);
                double volume = Double.parseDouble(rowStr.split("~")[indexVolume]);
                String address = rowStr.split("~")[indexAddress];
                Coordinates<Double, Double> coord = new Coordinates<>(lat, lon);
                String schedule = rowStr.split("~")[indexSchedule];
                String city = rowStr.split("~")[indexCityName];
                double grubType = Double.parseDouble(rowStr.split("~")[indexTypeOfGrub]);


//            System.out.println(String.format("size= %s;data= %s;", containers.size()+1, nextM.getTime()));

                byte[] hotPointSchedule = parseSchedule(schedule,nextM);
                if(city.toLowerCase().contains("ангар") & volume!=0 & hotPointSchedule[numberDay] == 1){
                    containers.add(new Container(address, coord,volume,(int)count,hotPointSchedule,grubType));
                }
            }

        }
        workbook.close();
        fis.close();

        return containers;
    }

    protected List<Polygon> getPolygons() throws IOException {
        // Получаем первый лист Excel файла
        XSSFSheet sheet = createExcelHandler(Storage.Polygons);
        List<Polygon> polygons = new ArrayList<>();
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
            polygons.add(new Polygon((int)id, address, coord));
        }
        workbook.close();
        fis.close();

        return polygons;
    }

    /**
     * Метод предназначенный для преобразования текста в число, если это возможно.
     * @param text Текст, который необходимо преобразовать в число
     * @return Число или 0
     */
    private static Integer tryParseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /**
     * Метод предназначенный для получения номера дня недели
     * @param day Название дня недели
     * @return Порядковый номер дня недели
     */
    private int getNumberDayOnWeek(String day){
        int numDay = 0;
        day = day.toLowerCase();
        if(day.contains("пон") || day.contains("пн")){
            numDay = 1;
        }
        else if(day.contains("втор") || day.contains("вт")){
            numDay = 2;
        }
        else if(day.contains("сред") || day.contains("ср")){
            numDay = 3;
        }
        else if(day.contains("чет") || day.contains("чт")){
            numDay = 4;
        }
        else if(day.contains("пят")|| day.contains("пт")){
            numDay = 5;
        }
        else if(day.contains("суб")|| day.contains("сб")){
            numDay = 6;
        }
        else if(day.contains("вос")|| day.contains("вс")){}
        return numDay;
    }

    /**
     * Метод предназначенный для создания OneHot представления.
     * OneHot представление: массив состоящий из бинарных элементов, где 0 - отсутствие необходимости вывоза, 1 - необходимость вывоза.
     * @param cnt Порядковый номер дня. Например, второй четверг месяца (cnt = 2)
     * @param combineDay Массив слов комбинированного графика вывоза [второй, четверг, месяца]
     * @param nextMonth Объект следуюшего месяца
     * @return Индекс дня месяца, в который необходимо осуществить вывоз
     */
    private int createOneHot(List<String> combineDay,  Calendar nextMonth, int cnt){
        int numDay = 0;
        if (combineDay.contains("Каждый")) {
            numDay = getNumberDayOnWeek(combineDay.get(2));
        }else {
            numDay = getNumberDayOnWeek(combineDay.get(1));
        }
        int countDays = nextMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        int count = 0;
        int oneHotIndex = 0;
        Date currentDate;
        for(int i = 0; i<=countDays;i++) {
            currentDate = nextMonth.getTime();

            if (numDay == currentDate.getDay()) {
                count++;
                nextMonth.add(Calendar.DATE, 1);

            } else {
                nextMonth.add(Calendar.DATE, 1);
            }
            if (count==cnt){
                oneHotIndex = currentDate.getDate();
                break;
            }
        }
        nextMonth.add(Calendar.DATE, -(oneHotIndex));
        return oneHotIndex;
    }

    /**
     * Метод предназначенный для преобразования графика вывоза к единому виду.
     * Виды записи графика вывоза:
     * <ol>
     *     <li>Числовые значения, например: 1,2, 15 - вывоз осуществляется в конкретные дни месяца</li>
     *     <li>Краткая запись, например: ПН, ПТ - вывоз осуществляется каждый указанный день недели на протяжении всего месяца</li>
     *     <li>Комбинированная запись, например: второй четверг месяца - вывоз осуществляется в день определенного порядка</li>
     * </ol>
     * @param schedule Запись о графике вывоза
     * @param nextMonth Объект следуюшего месяца
     * @return byte[] - One hot представление
     */
    private byte[] parseSchedule(String schedule, Calendar nextMonth){
        int countDays = nextMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        String[] partOfSchedule = schedule.split(",");
        List<String> newSchedulePart = new ArrayList<>();
        for (String str : partOfSchedule) {
            str = str.trim();
            newSchedulePart.add(str);
        }
        newSchedulePart.removeIf(String::isEmpty);

        byte[] oneHot = new byte[countDays];
        for(String day: newSchedulePart){
//            System.out.println("day:"+day+".");

            int numOfDay = tryParseInt(day);
            List<String> combineDay = new ArrayList<String>(Arrays.asList(day.split(" ")));;
            combineDay.removeAll(Arrays.asList("", null));
            if(numOfDay != 0 & numOfDay<=countDays){
                oneHot[numOfDay-1] = 1;
            }else if(day.toLowerCase().contains("ежед") | combineDay.size() == 7){
                Arrays.fill(oneHot, (byte)1);
            }
            else if(combineDay.size()==1) {
                try {
                    int numberOfWeek = getNumberDayOnWeek(day);
                    for(int i = 0; i<countDays-1;i++) {
                        Date currentDate = nextMonth.getTime();
                        if (numberOfWeek == currentDate.getDay()) {
                            oneHot[currentDate.getDate() - 1] = 1;
                        }
                        nextMonth.add(Calendar.DATE, 1);
                    }
                    nextMonth.add(Calendar.DATE, -(nextMonth.getTime().getDate()-1));

                }catch (Exception e){
                    System.out.println();
                }
            }
            else if (day.toLowerCase().contains("перв") | combineDay.get(1).contains("1")){
                oneHot[createOneHot(combineDay,nextMonth,1) - 1] = 1;
            } else if (day.toLowerCase().contains("второ")| combineDay.get(1).contains("2")) {
                oneHot[createOneHot(combineDay,nextMonth,2) - 1] = 1;
            } else if (day.toLowerCase().contains("трет") | combineDay.get(1).contains("3")) {
                oneHot[createOneHot(combineDay,nextMonth,3) - 1] = 1;
            }else if(day.toLowerCase().contains("четверт") | combineDay.get(1).contains("4")){
                oneHot[createOneHot(combineDay,nextMonth,4) - 1] = 1;
            }else if (day.toLowerCase().contains("заяв")) { }

        }
        return oneHot;
    }

    /**
     * Метод предназначенный для получения следующего месяца
     * @param date Текущая дата
     * @return Дата первое число следующего месяца
     */
    public static Calendar getNextMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
        } else {
            calendar.roll(Calendar.MONTH, true);
        }

        calendar.add(Calendar.DATE, -(int)calendar.getTime().getDate()+1);
        return calendar;
    }

    /**
     * Метод предназначенный для получения информации о всех машинах.
     * Требования к обрабатываемому документу:
     * <ol>
     *     <li>Остутствие заглавия документа (чтение начинается с ячейки A1)</li>
     *     <li>Столбец с индексом indexNumber - гос. номер машины</li>
     *     <li>Столбец с индексом indexTypeLoad - тип погрузки</li>
     *     <li>Столбец с индексом indexGarage - идентификатор гаража</li>
     *     <li>Столбец с индексом indexLoadCapacity - грузоподъемность</li>
     *     <li>Столбец с индексом indexFuel - тип топлива</li>
     *     <li>Столбец с индексом indexSchedule - график работы</li>
     * </ol>
     * @return Список объектов типа Car
     * @throws IOException Ошибки возникшие при чтении файла
     */
    private List<Car> getCars() throws IOException {
        // Получаем первый лист Excel файла
        XSSFSheet sheet = createExcelHandler(Storage.Cars);
        List<Car> cars = new ArrayList<>();
        String rowStr = "";
        int countRows = 0;
        for (Row row : sheet) {
            if (countRows == 0) {
                countRows ++;
            }
            else {
                countRows++;
                rowStr = iterateRow(row);
                int indexNumber = 2;
                int indexTypeLoad = 7;
                int indexGarage = 12;
                int indexVolume = 13;
                int indexCompactionRatio= 14;
                int indexLoadCapacity= 15;
                int indexFuel = 17;
                int indexSchedule = 18;
                int indexConsum = 27;

                String number = rowStr.split("~")[indexNumber];
                Double loadType = Double.parseDouble(rowStr.split("~")[indexTypeLoad]);
                double garageId = Double.parseDouble(rowStr.split("~")[indexGarage]);
                double capacity = Double.parseDouble(rowStr.split("~")[indexLoadCapacity]);
                double volume = Double.parseDouble(rowStr.split("~")[indexVolume]);
                double compactionRatio = Double.parseDouble(rowStr.split("~")[indexCompactionRatio]);
                String fuelType = rowStr.split("~")[indexFuel];
                String schedule = rowStr.split("~")[indexSchedule];
                double fuelConsum  =  Double.parseDouble(rowStr.split("~")[indexConsum]);
                if(volume==0){
                    continue;
                }else{
                    cars.add(new Car(number, capacity,loadType,garageId,fuelType,schedule,fuelConsum,compactionRatio,volume));
                }
            }
        }
        workbook.close();
        fis.close();

        return cars;
    }
    public static void create_csv(List<String> args) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("test.csv"),StandardOpenOption.APPEND,
                StandardOpenOption.CREATE)) {
            writer.write('\ufeff');
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

            for(String row: args){
                List<String> items = Arrays.asList(row.split(","));
                csvPrinter.printRecord(items.get(0),items.get(1),items.get(2),items.get(3),items.get(4));
            }

            csvPrinter.flush();
        }
    }

    /**
     * Метод предназначенный для итерации внутри строки.
     * @return Строковое значение всех элемнтов строки документа. Разделитель "~"
     */
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
