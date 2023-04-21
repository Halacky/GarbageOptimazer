package org.example;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Timer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * <h1>Класс для работы с данными</h1>
 * Данный класс содержит поля и методы, позволяющие обрабатывать входные данные.
 * На текущий момент возможно считывать данные из Excel, строго определенной структуры.
 * Необходимая структура описывается в каждом методе отдельно.
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
     * Метод для получения информации обо всех гаражах из файла Excel.
     * <p>
     * Требования к обрабатываемому документу:
     * - Столбец с индексом indexAddress содержит адрес гаража
     * - Столбец с индексом indexLat содержит широту гаража
     * - Столбец с индексом indexLon содержит долготу гаража
     * - Столбец с индексом indexGarageID содержит идентификатор гаража
     *
     * @return Список объектов типа Place, содержащий информацию обо всех гаражах из файла Excel
     * @throws IOException Ошибка, возникшая при чтении файла Excel
     */
    private List<Place> getGarage() throws IOException {
        // Получаем первый лист Excel файла
        XSSFSheet sheet = createExcelHandler(Storage.Garage);

        List<Place> places = new ArrayList<>();
        String rowStr = "";
        int idd = 0;
        for (Row row : sheet) {
            rowStr = iterateRow(row);

            // Задаем индексы для столбцов, содержащих адрес, широту, долготу и идентификатор гаража
            int indexLat = 2;
            int indexLon = 3;
            int indexAddress = 1;
            int indexGarageID = 0;

            // Получаем значения из строки
            double lat = Double.parseDouble(rowStr.split("~")[indexLat]);
            double lon = Double.parseDouble(rowStr.split("~")[indexLon]);
            double index = Double.parseDouble(rowStr.split("~")[indexGarageID]);
            String address = rowStr.split("~")[indexAddress];

            Coordinates<Double, Double> coord = new Coordinates<>(lat, lon);

            // Если гараж Иркутский или Ангарский, добавляем информацию о гараже в список
            if (index == 6 || index == 5 || index == 0) {
                places.add(new Place(idd, address, coord, index));
                idd++;
            }
        }

        // Закрываем потоки
        workbook.close();
        fis.close();

        return places;
    }

    /**
     * Метод fillGarage() предназначен для наполнения гаражей информацией об находящихся в них машинах.
     *
     * @return Список объектов типа Place, содержащий информацию о гаражах с машинами.
     * @throws IOException Ошибки, которые могут возникнуть при чтении файла.
     */
    public List<Place> fillGarage() throws IOException {
        // Получаем список всех гаражей.
        List<Place> places = getGarage();
        // Получаем список всех машин.
        List<Car> cars = getCars();
        // Проходим по каждому гаражу.
        for (Place place : places) {
            // Создаем временный список машин, которые находятся в этом гараже.
            List<Car> tmpCars = new ArrayList<>();
            // Проходим по каждой машине.
            for (Car car : cars) {
                // Если машина находится в этом гараже.
                if (place.getGarageIndex() == car.getGarageId()) {
                    // Устанавливаем координаты гаража для этой машины.
                    car.setCoordinates(place.getCoordinates());
                    // Добавляем машину во временный список.
                    tmpCars.add(car);
                }
            }
            // Устанавливаем список машин для этого гаража.
            place.setCars(tmpCars);
        }

        // Возвращаем список всех гаражей с машинами.
        return places;
    }

    /**
     * Метод предназначенный для получения информации о всех контейнерах.
     * Требования к обрабатываемому документу:
     * <ol>
     *     <li>Заголовок в документе должен быть размером в одну строку (чтение начинается с А2)</li>
     *     <li>Столбец с индексом indexCityName - город нахождения КП</li>
     *     <li>Столбец с индексом indexAddress - адрес контейнера</li>
     *     <li>Столбец с индексом indexLat - широта контейнера</li>
     *     <li>Столбец с индексом indexLon - долгота контейнера</li>
     *     <li>Столбец с индексом indexCount - количество контейнеров</li>
     *     <li>Столбец с индексом indexVolume - объем контейнера</li>
     *     <li>Столбец с индексом indexSchedule - график вывоза КП</li>
     *     <li>Столбец с индексом indexTypeOfGrub - тип захвата</li>
     * </ol>
     *
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
                countRows++;
            } else {
                rowStr = iterateRow(row);

                int indexCityName = 0;
                int indexCode = 2;
                int indexAddress = 3;
                int indexLat = 10;
                int indexLon = 11;
                int indexCount = 12;
                int indexVolume = 13;
                int indexSchedule = 14;
                int indexTypeOfGrub = 22;

                double lat = Double.parseDouble(rowStr.split("~")[indexLat]);
                double lon = Double.parseDouble(rowStr.split("~")[indexLon]);
                double count = Double.parseDouble(rowStr.split("~")[indexCount]);
                double volume = Double.parseDouble(rowStr.split("~")[indexVolume]);
                String address = rowStr.split("~")[indexAddress].replaceAll("\n", "");
                Coordinates<Double, Double> coord = new Coordinates<>(lat, lon);
                String schedule = rowStr.split("~")[indexSchedule];
                String city = rowStr.split("~")[indexCityName];
                String code = rowStr.split("~")[indexCode];
                double grubType = Double.parseDouble(rowStr.split("~")[indexTypeOfGrub]);
                nextM = getNextMonth(new Date());

                byte[] hotPointSchedule = parseSchedule(schedule, nextM);
                if(city.toLowerCase().contains("ангар") & volume!=0 & hotPointSchedule[numberDay] == 1){
                    containers.add(new Container(code, address, coord,volume,(int)count,hotPointSchedule,grubType));

                }
            }

        }
        workbook.close();
        fis.close();

        return containers;
    }

    /**
     * Получает список полигонов из Excel-файла
     *
     * @return Список полигонов
     * @throws IOException Если возникли проблемы с чтением файла
     */
    protected List<Polygon> getPolygons() throws IOException {
        // Получаем первый лист Excel файла
        XSSFSheet sheet = createExcelHandler(Storage.Polygons);

        List<Polygon> polygons = new ArrayList<>();
        String rowStr = "";

        // Итерируемся по каждой строке в листе
        for (Row row : sheet) {
            // Получаем строку в виде строки "~"-разделителей
            rowStr = iterateRow(row);

            // Получаем индексы нужных колонок
            int indexLat = 2;
            int indexLon = 3;
            int indexAddress = 1;
            int indexPolygonID = 0;

            // Извлекаем значения из строки
            double lat = Double.parseDouble(rowStr.split("~")[indexLat]);
            double lon = Double.parseDouble(rowStr.split("~")[indexLon]);
            double id = Double.parseDouble(rowStr.split("~")[indexPolygonID]);
            String address = rowStr.split("~")[indexAddress];

            // Создаем объект координат и полигон, добавляем его в список
            Coordinates<Double, Double> coord = new Coordinates<>(lat, lon);
            polygons.add(new Polygon((int) id, address, coord));
        }

        // Закрываем Excel-файл и поток чтения
        workbook.close();
        fis.close();

        // Возвращаем список полигонов
        return polygons;
    }


    /**
     * Метод предназначенный для преобразования текста в число, если это возможно.
     *
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
     *
     * @param day Название дня недели
     * @return Порядковый номер дня недели
     */
    private int getNumberDayOnWeek(String day) {
        int numDay = 0;
        day = day.toLowerCase();
        if (day.contains("пон") || day.contains("пн")) {
            numDay = 1;
        } else if (day.contains("втор") || day.contains("вт")) {
            numDay = 2;
        } else if (day.contains("сред") || day.contains("ср")) {
            numDay = 3;
        } else if (day.contains("чет") || day.contains("чт")) {
            numDay = 4;
        } else if (day.contains("пят") || day.contains("пт")) {
            numDay = 5;
        } else if (day.contains("суб") || day.contains("сб")) {
            numDay = 6;
        } else if (day.contains("вос") || day.contains("вс")) {
        }
        return numDay;
    }

    /**
     * Метод предназначенный для создания OneHot представления.
     * OneHot представление: массив состоящий из бинарных элементов, где 0 - отсутствие необходимости вывоза, 1 - необходимость вывоза.
     *
     * @param cnt        Порядковый номер дня. Например, второй четверг месяца (cnt = 2)
     * @param combineDay Массив слов комбинированного графика вывоза [4,четверг]
     * @param nextMonth  Объект следуюшего месяца
     * @return Индекс дня месяца, в который необходимо осуществить вывоз
     */
    private int getDayOfMonthForOccurrence(List<String> combineDay, Calendar nextMonth, int cnt) {
        int numDay = 0;
        if (combineDay.contains("Каждый")) {
            numDay = getNumberDayOnWeek(combineDay.get(2));
        } else {
            numDay = getNumberDayOnWeek(combineDay.get(1));
        }
        int countDays = nextMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        int count = 0;
        int oneHotIndex = 0;
        Date currentDate;
        // проходим по всем дням месяца и ищем день недели, который встречается cnt раз
        for (int i = 0; i <= countDays; i++) {
            currentDate = nextMonth.getTime();
            if (numDay == currentDate.getDay()) {
                count++;
                nextMonth.add(Calendar.DATE, 1);
            } else {
                nextMonth.add(Calendar.DATE, 1);
            }
            if (count == cnt) {
                oneHotIndex = currentDate.getDate();
                break;
            }
        }
        // возвращаем индекс в one-hot представлении
        nextMonth.add(Calendar.DATE, -(oneHotIndex));
        return oneHotIndex;
    }

    private List<String> getMatches(String input) {
        input = input.replaceAll("-[а-яА-Я]", "");
        input =  input.replaceAll(" ", "");
        String[] daysOfWeek = {"понедельник", "вторник", "среда", "четверг", "пятница", "суббота", "воскресенье"};
        List<String> res = new ArrayList<>();
        while (!input.trim().isEmpty()) {
            for (String dayOfWeek : daysOfWeek) {
                int index = input.toLowerCase().indexOf(dayOfWeek);
                if (index != -1) {
                    String numbers = input.substring(0, index).trim();
                    if (!numbers.isEmpty()) {
                        String[] numberList = numbers.split(",");
                        for (String number : numberList) {
                            if(!number.equals("")){
                                String row = number + ":" + dayOfWeek;
//                                System.out.println(row);
                                res.add(row);

                            }
                        }
                    }
                    input = input.substring(index + dayOfWeek.length()).trim();
                    break;
                }
            }
        }
        return res;
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
    private byte[] parseSchedule(String schedule, Calendar nextMonth) {
        int countDays = nextMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        String[] partOfSchedule = schedule.split(",");
        List<String> newSchedulePart = Arrays.stream(partOfSchedule)
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .toList();

        byte[] oneHot = new byte[countDays];

        if(schedule.contains("-")){
            if(!(schedule.equals("Не определен") | schedule.equals("По запросу"))){
                List<String> partSc  = getMatches(schedule);
                for(String  sc: partSc){
                    int cnt = Integer.parseInt(sc.split(":")[0]);
                    oneHot[getDayOfMonthForOccurrence(List.of(sc.split(":")), nextMonth, cnt) - 1] = 1;
                }
            }
        }else{
            for (String day : newSchedulePart) {
                nextMonth = getNextMonth(new Date());
                int numOfDay = tryParseInt(day);
                if (numOfDay > 0 && numOfDay <= countDays) {
                    oneHot[numOfDay - 1] = 1;
                } else if (day.toLowerCase().contains("ежед") || newSchedulePart.size() == 7) {
                    Arrays.fill(oneHot, (byte) 1);
                }else {
                    try {
                        int numberOfWeek = getNumberDayOnWeek(day);
                        for (int i = 0; i < countDays; i++) {
                            Date currentDate = nextMonth.getTime();
                            if (numberOfWeek == currentDate.getDay()) {
                                oneHot[currentDate.getDate() - 1] = 1;
                            }
                            nextMonth.add(Calendar.DATE, 1);
                        }
                        nextMonth.add(Calendar.DATE, -(nextMonth.getTime().getDate() - 1));

                    } catch (Exception e) {
                        System.out.println();
                    }
                }
            }
        }


        return oneHot;
    }

    /**
     * This method returns the next month's first day from a given date.
     *
     * @param date The date to get the next month from.
     * @return The first day of the next month.
     */
    public static Calendar getNextMonth(Date date) {
        // Create a new Calendar instance and set it to the provided date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // If the current month is December, set the next month to January of the next year
        if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
        } else {
            // Otherwise, roll the calendar to the next month
            calendar.roll(Calendar.MONTH, true);
        }

        // Set the day of the month to the first day of the next month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
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
     *     <li>Столбец с индексом indexVolume - объем кузова</li>
     *     <li>Столбец с индексом indexFuel - тип топлива</li>
     *     <li>Столбец с индексом indexSchedule - график работы</li>
     *     <li>Столбец с индексом indexCompactionRatio - коэффициент сжатия</li>
     *     <li>Столбец с индексом indexConsum - потребление топлива</li>
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
                int indexLoadCapacity= 15;
                int indexVolume = 13;
                int indexCompactionRatio= 14;
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
                if(volume!=0){
                    cars.add(new Car(number, capacity,loadType,garageId,fuelType,schedule,fuelConsum,compactionRatio,volume));
                }
            }
        }
        workbook.close();
        fis.close();

        return cars;
    }

    /**
     Метод создания CSV-файла на основе списка строк.
     @param args Список строк, содержащих данные для записи в CSV-файл
     @throws IOException Ошибки возникшие при записи в файл
     */
    public static void create_csv(List<String> args) throws IOException {
        // Указываем имя файла и разделитель столбцов
        String filename = "example.csv";
        String delimiter = ",";
        // Задаем заголовок для CSV-файла
        String[] header = {"Lat", "Lon", "Descr", "Sub", "Num"};

        try (FileOutputStream fos = new FileOutputStream(new File(filename), true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            BufferedWriter writer = new BufferedWriter(osw)) {
            // Проверяем, пустой ли файл. Если да, записываем заголовок
            if (fos.getChannel().size() == 0) {
                for (int i = 0; i < header.length; i++) {
                    writer.append(header[i]);
                    if (i != header.length - 1) {
                        writer.append(delimiter);
                    }
                }
                writer.append("\n");
            }
            // Записываем данные из списка в CSV-файл
            for (String row: args) {
                String[] splitedRow = row.split(",");
                writer.append(splitedRow[0]+ delimiter);
                writer.append(splitedRow[1]+delimiter);
                writer.append(splitedRow[2] +delimiter);
                writer.append(splitedRow[3]+delimiter);
                writer.append(splitedRow[4]);
                writer.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
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
