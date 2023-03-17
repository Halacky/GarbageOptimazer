package org.example;

import com.grum.geocalc.Coordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import static org.example.DataHandler.create_csv;

/**
 * <h1>Основной класс алгоритма</h1>
 * Данный класс содержит методы описывающие логику алгоритма.
 * @author  Головань Кирилл
 * @version 1.1
 * @since   2023-01-23
 */
public class GarbageOptimizer {
    static Logger LOGGER;
    static {
        try(FileInputStream ins = new FileInputStream("C:\\Users\\RomeF\\OneDrive\\Документы\\GitHub\\GarbageOptimazer\\src\\main\\GarbageOptimizerLogging.properties")){
            LogManager.getLogManager().readConfiguration(ins);
            LOGGER = Logger.getLogger(GarbageOptimizer.class.getName());
        }catch (Exception ignore){
            ignore.printStackTrace();
        }
    }
    private List<Place> places; // список гаражей
    private List<Container> AllContainers; // Список всех контейнеров
    private List<Container> CurrentDayContainers; // Список контейнеров
    private List<List<Double>> DistanceMatrix; // Матрица расстояний
    private List<Container> AllServiceCont;
    private List<Polygon> Polygons;

    public GarbageOptimizer(int numberDay) throws IOException {
        CurrentDayContainers = new ArrayList<>();
        createDistanceMatrix(numberDay);
        AllServiceCont = new ArrayList<>();
        Polygons = new DataHandler().getPolygons();
//        writeCurrentToCsv();
    }

    /**
     * Метод предназначенный для подсчета метрик М1, М2, М3.
     * @param car - объект мышины, для которой необходимо посчитать метрики
     * @param s - расстояние, которое необходимо проехать машине
     * @return Double[] - массив метрик.
     */
    private double[] calculateMMetrix(Car car, double s){
        double M1 = car.getFuelConsum();
        double M2 = M1 / car.getFreeVolume();
        double M3 = M2*1/s;
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
    public List<List<Double>> createDistanceMatrix(int numberDay) throws IOException {
        LOGGER.log(Level.INFO,"Номер дня "+ numberDay);
        DataHandler dh = new DataHandler();
        places = dh.fillGarage();
        AllContainers = dh.getContainers(numberDay);
        List<List<Double>> distanceMatrix = new ArrayList<>();
        for (Place place : places) {
            List<Double> row = new ArrayList<>();

            for (Container container : AllContainers) {
                if(container.getSchedule()[numberDay] == 1){

                    double distance =  calculateDistance(place.getCoordinates(),container.getCoordinates());//in meters
                    row.add(distance);
                    if (!CurrentDayContainers.contains(container)) CurrentDayContainers.add(container);
                }
            }
            distanceMatrix.add(row);
        }
        DistanceMatrix = distanceMatrix;
        return distanceMatrix;
    }

    /**
     * Метод предназначенный для рассчета расстония между двумя точками.
     * @param from Исходная точка
     * @param to Конечная точка
     * @return  Double - расстояниме между точками.
     */
    protected double calculateDistance(Coordinates from, Coordinates to) throws IOException {
        GraphhopperPathDetails gh = new GraphhopperPathDetails();
        gh.createRouteV2(from, to);
        return gh.getDistance();
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

    private void writeGaragesToCsv() throws IOException {
        List<String> toCsv = new ArrayList<>();
        int cnt = 1;
        for(Place place : places){

            String row = place.getCoordinates().getLongitude()+","+ place.getCoordinates().getLatitude()+","+"Place,"+ place.getAddress().replaceAll(",","~")+","+cnt;
            cnt++;
            toCsv.add(row);

        }
        create_csv(toCsv);
    }

    private void writeCurrentToCsv() throws IOException {
        List<String> toCsv = new ArrayList<>();
        int cnt = 1;
        for(Container garage: CurrentDayContainers){

            String row = garage.getCoordinates().getLongitude()+","+ garage.getCoordinates().getLatitude()+","+"Current,"+garage.getAddress().replaceAll(",","~")+","+cnt;
            cnt++;
            toCsv.add(row);

        }
        create_csv(toCsv);
    }

    private void writeContainersToCsv(Car bestCar) throws IOException {
        int garageIndex = 0;
        for (Place place : places){
            if (bestCar.getGarageId() == place.getGarageIndex()) {
                garageIndex = (int) place.getId();
            }
        }
        if (garageIndex == -1)
        {
            garageIndex = DistanceMatrix.size()-1;
        }
        List<String> toCsv = new ArrayList<>();
        int cnt = 1;
        for(Container container: bestCar.getServicesContainers()){
            String row = container.getCoordinates().getLongitude()+","+ container.getCoordinates().getLatitude()+"," +Place.getPlaceById(garageIndex, places).getAddress().replaceAll(" ", "")+"|"+bestCar.getNumber()+ ","+container.getAddress().replaceAll(",","~")+","+cnt;
            cnt++;
            toCsv.add(row);
        }
        create_csv(toCsv);
    }

    /**
     * Метод предназначенный для нахождения саммой удаленной точки назначения.
     */
    protected void findFcy() throws IOException {
        writeGaragesToCsv();
        while (CurrentDayContainers.size()!=0){
            int indexOfFcy = getIndexOfLargest(sumDistance());

            if (CurrentDayContainers.get(indexOfFcy).getIsCater()) continue;
            List<Place> carPlaces = getOptimalCarPlace(indexOfFcy);
            Car bestCar = null;
            for (int i=0; i<3;i++){
                bestCar = getBestCar(carPlaces.get(i),indexOfFcy);
                if (bestCar!=null){
                    break;
                }
            }
            if (bestCar==null){
                break;
            }
            addRowInMatrix(CurrentDayContainers.get(indexOfFcy).getCoordinates());

            servicingContainer(bestCar,indexOfFcy);

            DistanceMatrix.remove(DistanceMatrix.size()-1);

            writeContainersToCsv(bestCar);

        }
        DistanceMatrix.remove(DistanceMatrix.size()-1);
        LOGGER.log(Level.INFO,"~~~~~~~~~~~~~Car is left~~~~~~~~~~~~~~");
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
        CurrentDayContainers.remove(index);
    }

    private boolean checkCarCapacity(Car bestCar, int index){
        return bestCar.getFreeVolume() >= CurrentDayContainers.get(index).getAllVolume()/bestCar.getCompactionRatio();
    }
    private boolean checkWorkingTime(Car bestCar){
        return bestCar.getTimeInWork()<=15;
    }

    /**
     * Метод предназначенный для добавления строки в матрицу расстояний.
     * @param objF Исходная точка
     */
    private void addRowInMatrix(Coordinates objF) throws IOException {
        List<Double> row = new ArrayList<>();
        for (Container container : CurrentDayContainers) {
            double distance = calculateDistance(objF, container.getCoordinates());//in meters
            row.add(distance);

        }
        DistanceMatrix.add(row);

    }

    /**
     * Метод предназначенный для нахождения лучшей машины среди выбранных гаражей.
     * @param optimalGarages Список выбранных (по удаленности) гаражей
     * @return  Car - объект лучшей машины.
     */
    private Car getBestCar(Place optimalGarages, int indexOfFcy){
        double tmpMinM = Double.MAX_VALUE;
        double distance;
        Car bestCar = null;

        for (Car car: optimalGarages.getCars()){
            int garageIndex = 0;
            for (Place place : places){
                if (car.getGarageId() == place.getGarageIndex()) garageIndex = (int) place.getId();
            }
            if (garageIndex == -1) garageIndex = DistanceMatrix.size()-1;

            distance = DistanceMatrix.get(garageIndex).get(indexOfFcy);

            if (checkCarCapacity(car, indexOfFcy) & !car.isInWork() & checkWorkingTime(car)){
                calculateMMetrix(car,distance);
                if (car.getM3() < tmpMinM){
                    bestCar = car;
                    tmpMinM = bestCar.getM3();
                }
            }
        }

        return bestCar;
    }

    private void getNeighbors(Car car) throws IOException {

        int indexNearest = findNearest(car);
        servicingContainer(car, indexNearest);
    }

    private int findNearest(Car bestCar){
        List<Double> parent = DistanceMatrix.get(DistanceMatrix.size()-1);
        double min = Double.MAX_VALUE;
        int indexMin = 0;
        for(int i = 0; i<parent.size();i++){
            if(parent.get(i) != 0){
                if (min>parent.get(i) & CurrentDayContainers.get(i).getAllVolume()<=bestCar.getFreeVolume()){
                    min = parent.get(i);
                    indexMin = i;
                }
            }

        }
        return indexMin;
    }
    private void checkProximityToCentroid(int bestPolygonIndex, Car bestCar){
        //TODO Найти ближайшие КП по близости центроиды
        Polygon polygon = Polygons.get(bestPolygonIndex);
        Container lastServiceContainer = bestCar.getServicesContainers().get(bestCar.getServicesContainers().size()-1);
        for (Coordinates coordinate: getCheckpoints(lastServiceContainer.getCoordinates(),polygon.getCoordinates())){
            for(Container container:CurrentDayContainers){
                if (!container.getIsCater()){

                }
            }
        }

    }

    private List<Coordinates> getCheckpoints(Coordinates from, Coordinates to){
        double xF = (double) from.getLatitude();
        double xT = (double) to.getLatitude();
        double yF = (double) from.getLongitude();
        double yT = (double) from.getLongitude();
        double m = (yT-yF)/(xT-xF);
        List<Coordinates> coordinates = new ArrayList<>();
        double xMin = Math.min((double) from.getLatitude(),(double) to.getLatitude());
        double xMax = Math.max((double) from.getLatitude(),(double) to.getLatitude());
        double yMin = Math.min((double) from.getLongitude(),(double) to.getLongitude());
        for (double i=xMin; i<=xMax; i+=(xMax-xMin)/10){
            double yTmp = yMin + m * (i - xF);
            coordinates.add(new Coordinates<>(yTmp, i));

        }
        return coordinates;
    }

    private void servicingContainer(Car car, int indexOfFcy) throws IOException {
        if(CurrentDayContainers.size()>0){
            if(checkWorkingTime(car)){
                if(checkCarCapacity(car,indexOfFcy)){
                    LOGGER.log(Level.INFO,"~~~~~~~~~~~~~servicingContainer~~~~~~~~~~~~~~");
                    int garageIndex = 0;
                    for (Place place : places){
                        if (car.getGarageId() == place.getGarageIndex()) {
                            garageIndex = (int) place.getId();
                        }
                    }
                    if (garageIndex == -1)
                    {
                        garageIndex = DistanceMatrix.size()-1;
                    }
                    LOGGER.log(Level.INFO,"Самая удаленная КП: " + CurrentDayContainers.get(indexOfFcy).getAddress());
                    Container container = CurrentDayContainers.get(indexOfFcy);
                    LOGGER.log(Level.INFO,"Гараж: "+ car.getGarageId()+"; "+ Place.getPlaceById(garageIndex, places).getAddress());
                    LOGGER.log(Level.INFO,"Машина: "+ car.getNumber());
                    LOGGER.log(Level.INFO,"Время в работе ДО: "+ car.getTimeInWork());
                    container.setCarNumber(car.getNumber());
                    car.setServicesContainers(container);
                    container.setIsCater(true);

                    Double distance = DistanceMatrix.get(garageIndex).get(indexOfFcy);
                    calculateMMetrix(car, distance);
                    calculateWorkingTime(car, distance, Math.ceil(container.getAllVolume()));
                    LOGGER.log(Level.INFO,"Время в работе ПОСЛЕ: "+ car.getTimeInWork());
                    double longCentroid = 0;
                    double latCentroid = 0;
                    for (Container serviceCont: car.getServicesContainers()){
                        longCentroid += serviceCont.getCoordinates().getLongitude();
                        latCentroid += serviceCont.getCoordinates().getLatitude();
                    }
                    Coordinates<Double, Double> centroid = new Coordinates<>(
                            longCentroid/car.getServicesContainers().size(),
                            latCentroid/car.getServicesContainers().size()
                    );
                    LOGGER.log(Level.INFO,"Объем машины ДО: " + car.getFreeVolume());

                    LOGGER.log(Level.INFO,"Объем контейнера: " + container.getAllVolume());
                    if (car.getCompactionRatio() != 0){
                        car.setFreeVolume(car.getFreeVolume()-container.getAllVolume()/car.getCompactionRatio());
                    }else{
                        car.setFreeVolume(car.getFreeVolume()-container.getAllVolume());
                    }
                    LOGGER.log(Level.INFO,"Объем машины ПОСЛЕ: " + car.getFreeVolume());

                    car.setCentroid(centroid);
                    car.setInWork(true);
                    AllServiceCont.add(container);
                    removeTemporaryOT(indexOfFcy);
                    getNeighbors(car);
                }else{
                    double bestPolygonIndex = getBestPolygon(car.getCentroid()).get(0);
                    double distanceToPolygon = getBestPolygon(car.getCentroid()).get(1);
                    Polygon bestPolygon = Polygons.get((int) bestPolygonIndex);
                    calculateWorkingTime(car, distanceToPolygon*2, Math.ceil(car.getVolume()-car.getFreeVolume()));
                    LOGGER.log(Level.INFO,"Время в работе ПОСЛЕ ПОЛИГОНА: "+ car.getTimeInWork());

                    car.setFreeVolume(car.getVolume());
                    car.setInWork(true);
                    getNeighbors(car);
                }
            }
        }

    }

    private void calculateWorkingTime(Car bestCar, double distance, double volume){
        double averageSpeed = 60;
        bestCar.setTimeInWork(bestCar.getTimeInWork()+((distance/1000)/averageSpeed) + volume*0.1);
    }

    /**
     * Метод предназначенный для нахождения лучших гаражей. Выбор происходит на основе сумм расстояний
     * @param fcy Индекс саммой удаленной точки, до неё будет искать суммы расстояний
     * @return  List Place - объекты самых ближайщих гаражей.
     */
    private List<Place> getOptimalCarPlace(int fcy){
        List<Place> optimalPlace = new ArrayList<>();
        List<Double> arr = new ArrayList<>();
        for (int j = 0; j<DistanceMatrix.size();j++) {
            double current = DistanceMatrix.get(j).get(fcy);
            places.get(j).setDistanceToFcy(current);
            arr.add(current);
        }
        Collections.sort(places);

//        sort(arr, 0, arr.size()-1);
//        for (int i=0;i<3;i++){
//            for (int j = 0; j<DistanceMatrix.size();j++) {
//                double current = DistanceMatrix.get(j).get(fcy);
//                if (arr.get(i) == current){
//                    optimalPlace.add(places.get(i));
//                }
//            }
//        }

        return places;
    }

    int partition(List<Double> arr, int low, int high)
    {
        Double pivot = arr.get(high);
        int i = (low-1); // index of smaller element
        for (int j=low; j<high; j++)
        {
            // If current element is smaller than or
            // equal to pivot
            if (arr.get(j) <= pivot)
            {
                i++;
                // swap arr[i] and arr[j]
                Double temp = arr.get(i);
                arr.set(i, arr.get(j));
                arr.set(j, temp);
            }
        }

        // swap arr[i+1] and arr[high] (or pivot)
        Double temp = arr.get(i + 1);
        arr.set(i + 1, arr.get(high));
        arr.set(high, temp);

        return i+1;
    }


    /* The main function that implements QuickSort()
      arr[] --> Array to be sorted,
      low  --> Starting index,
      high  --> Ending index */
    void sort(List<Double> arr, int low, int high)
    {
        if (low < high)
        {
            /* pi is partitioning index, arr[pi] is
              now at right place */
            int pi = partition(arr, low, high);

            // Recursively sort elements before
            // partition and after partition
            sort(arr, low, pi-1);
            sort(arr, pi+1, high);
        }
    }

    private List<Double> getBestPolygon(Coordinates<Double,Double> point) throws IOException {
        Coordinates<Double, Double> list = point;
        Coordinate lat = Coordinate.fromDegrees(list.getLatitude());
        Coordinate lng = Coordinate.fromDegrees(list.getLongitude());
        Point objFrom = Point.at(lat, lng);
        double minD = Double.MAX_VALUE;
        int index = 0;
        for (int i=0; i<Polygons.size();i++){
            lat = Coordinate.fromDegrees(Polygons.get(i).getCoordinates().getLatitude());
            lng = Coordinate.fromDegrees(Polygons.get(i).getCoordinates().getLongitude());
            Point objTo = Point.at(lat, lng);
            double distance = calculateDistance(point,Polygons.get(i).getCoordinates());
            if(distance<minD){
                minD = distance;
                index = i;
            }
        }
        List<Double> result = new ArrayList<>();
        result.add((double)index);
        result.add(minD);
        return result;
    }

    /**
     * Метод предназначенный для нахождения инедкса наибольшего значения в массиве.
     * @param array Массив чисел
     * @return  int - индекс наибольшего числа
     */
    private int getIndexOfLargest(List<Double> array )
    {
        if ( array == null || array.size() == 0 ) return -1;

        array.indexOf(Collections.max(array));
        int largest = array.indexOf(Collections.max(array));
//        for ( int i = 1; i < array.size(); i++ )
//        {
//            if ( array.get(i) > array.get(largest)) largest = i;
//        }
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
            double currentMetric = AllContainers.get(i).getAllVolume()/array.get(i);
            double maxMetric = AllContainers.get(smallest).getAllVolume()/array.get(smallest);
            if (currentMetric>maxMetric & array.get(i) > 0) {
                smallest = i;
            }
        }
        return smallest;
    }

    public void test() throws IOException {
        for (Place g : places) {
            for (Container container : CurrentDayContainers) {
                if (Objects.equals(container.getAddress(), "Ангарск, Южный массив, 34")){
                    double distance = calculateDistance(g.getCoordinates(), container.getCoordinates());//in meters
                    System.out.println();
                }
            }
        }

    }
}
// QA Что делать с Бодайбо, до них ехать 30+ часов, как их вывозят сейчас?
// QA Какие единицы измерения у значения объема КП?
// TODO не удаляются полигоны
//TODO Ограничить работу алгоритма до Иркутска и Ангарска
//TODO расчёт времени прибывания машины в работе
//TODO проверка близости центроиды при выборе машины
//TODO Ограничение по захвату
//TODO Ограничение по дате выгрузки


