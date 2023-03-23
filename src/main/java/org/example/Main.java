package org.example;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class Main
{
    public static void main( String[] args ) throws IOException {

        try {
            int countDays = DataHandler.getNextMonth(new Date()).getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 0; i<countDays;i++){
                GarbageOptimizer go = new GarbageOptimizer();
                go.findFcy(i);
//                go.test();

            }
            System.out.println();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
