package org.example;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main
{
    public static void main( String[] args )
    {
        try {
            List<List<Double>> garagesInfo = new GarbageOptimazer().createDistanceMatrix();

//            new DataHandler().fillGarage();
            System.out.println();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
