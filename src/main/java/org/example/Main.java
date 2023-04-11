package org.example;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main
{
    public static void main( String[] args ) throws IOException {

        try {
            int countDays = DataHandler.getNextMonth(new Date()).getActualMaximum(Calendar.DAY_OF_MONTH);
            GarbageOptimizer go = new GarbageOptimizer();

            for (int i = 0; i<countDays;i++){
                if(i==9){
                    System.out.println();
                }
                go.findFcy(i);
//                for (Place place : go.getPlaces()) {
//                    for (Car car : place.getCars()) {
//                        for (Container container : car.getServicesContainers()) {
//                            if(container.getCarNumber()!=null){
//                                System.out.println();
//                            }
//                        }
//                    }
//                }
//                go.test();

            }

            List<String> resRow = new ArrayList<>();
            List<String> resData = new ArrayList<>();
            try (FileWriter writer = new FileWriter("data.csv", StandardCharsets.UTF_8)) {
                writer.append("Address (Place);Number (Car);TypeOfGrab (Car);Address (Container);Coordinates (Container);TypeOfGrab (Container);Centroid;Sch; CNT\n");
                for (Place place : go.getPlaces()) {
                    for (Car car : place.getCars()) {
                        int cnt = 0;
                        for (Container container : car.getServicesContainers()) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < container.getSchedule().length; i++) {
                                if (container.getSchedule()[i] == 1) {
                                    if (sb.length() > 0) {
                                        sb.append("|");
                                    }
                                    sb.append(i+1);
                                }
                            }

                            String data = place.getAddress() + ";" + car.getNumber() + ";" + car.getTypeOfGrab() + ";"
                                    + container.getAddress().replaceAll(",", " ") + ";" + container.getCoordinates().getLatitude()+"|"+container.getCoordinates().getLongitude() + ";"
                                    + container.getTypeOfGrab() + ";" + car.getCentroid().getLatitude()+"|"+car.getCentroid().getLongitude() +";"+ sb;

                            String row = container.getCoordinates().getLatitude()+","+container.getCoordinates().getLongitude()+","+place.getAddress() + "|" + car.getNumber() + "|" + car.getTypeOfGrab() + "|"
                                    + container.getAddress().replaceAll(",", " ") + ","+ sb+ ","+cnt;

                            if (!resData.contains(data)) {
                                cnt ++;
                                resData.add(data);
                                data +=  ";" + cnt + "\n";
                                writer.append(data);
                            }

                            if(!resRow.contains(row)) {
                                resRow.add(row);

                            }
                        }
                    }
                }
                DataHandler.create_csv(resRow);
                System.out.println("Data written to CSV successfully");
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println();
    }
}
