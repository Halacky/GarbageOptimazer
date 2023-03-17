package org.example;

import org.json.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class GraphhopperPathDetails {

    private List<Coordinates> TemporaryCoord;

    private Double Distance;
    private Double Time;

    public Double getDistance() {
        return Distance;
    }

    public Double getTime() {
        return Time;
    }

    public void createRoute(Coordinates<Double,Double> from, Coordinates<Double,Double> to) throws IOException {
        double latFrom = from.getLatitude();
        double lonFrom = from.getLongitude();
        double latTo = to.getLatitude();
        double lonTo = to.getLongitude();
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("##.000000", decimalFormatSymbols);
//        System.out.println(latFrom+" "+lonFrom+" "+latTo+" "+lonTo); //1,237,516.25

        HttpClient client = HttpClientBuilder.create().build();
        String req = "https://graphhopper.com/api/1/route?point="+decimalFormat.format(latFrom)+
                ","+decimalFormat.format(lonFrom)+"&point="+decimalFormat.format(latTo) +","+decimalFormat.format(lonTo)
                +"&vehicle=truck&points_encoded=false&instructions=false&elevation=false&calc_points=true&key=d7de41b1-fc61-4b4d-be55-7fb703bac6b6";
        HttpGet request = new HttpGet(req);

        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
//            System.out.println(content);
            JSONObject jsonObject = new JSONObject(content);
            JSONArray pathsArray = jsonObject.getJSONArray("paths");

            for (int i = 0; i < pathsArray.length(); i++) {
                JSONObject path = pathsArray.getJSONObject(i);
                Distance = path.getDouble("distance");
                Time = path.getDouble("time");
                JSONArray coordinatesArray = path.getJSONObject("points").getJSONArray("coordinates");
                for (int j = 0; j < coordinatesArray.length(); j++) {
                    JSONArray coordinate = coordinatesArray.getJSONArray(j);
                    double longitude = coordinate.getDouble(0);
                    double latitude = coordinate.getDouble(1);
                    System.out.println("Distance: " + Distance + ", Time: " + Time + ", Longitude: " + longitude + ", Latitude: " + latitude);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createRouteV2(Coordinates<Double,Double> from, Coordinates<Double,Double> to) throws IOException {
        double latFrom = from.getLatitude();
        double lonFrom = from.getLongitude();
        double latTo = to.getLatitude();
        double lonTo = to.getLongitude();
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("##.00000", decimalFormatSymbols);
//        System.out.println(latFrom+" "+lonFrom+" "+latTo+" "+lonTo); //1,237,516.25

        HttpClient client = HttpClientBuilder.create().build();
        String req = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=5b3ce3597851110001cf6248748acac548614d5c8e0e523f9aa0a49d&start="+decimalFormat.format(lonFrom)+","+decimalFormat.format(latFrom)+"&end="+decimalFormat.format(lonTo)+","+decimalFormat.format(latTo);
//        String req = "https://graphhopper.com/api/1/route?point="+decimalFormat.format(latFrom)+
//                ","+decimalFormat.format(lonFrom)+"&point="+decimalFormat.format(latTo)+","+decimalFormat.format(lonTo)
//                +"&vehicle=truck&points_encoded=false&instructions=false&elevation=false&calc_points=true&key=d7de41b1-fc61-4b4d-be55-7fb703bac6b6";
        HttpGet request = new HttpGet(req);

        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            System.out.println(content);
            JSONObject featureCollection = new JSONObject(content);
            JSONArray features = featureCollection.getJSONArray("features");
            JSONObject feature = features.getJSONObject(0);
            JSONObject geometry = feature.getJSONObject("geometry");
            JSONArray coordinates = geometry.getJSONArray("coordinates");

//            System.out.println("Coordinates:");
            for (int i = 0; i < coordinates.length(); i++) {
                JSONArray point = coordinates.getJSONArray(i);
                double longitude = point.getDouble(0);
                double latitude = point.getDouble(1);
//                System.out.println("Latitude: " + latitude + ", Longitude: " + longitude);
            }

            JSONArray segments = feature.getJSONObject("properties").getJSONArray("segments");
            JSONObject segment = segments.getJSONObject(0);
            Distance = segment.getDouble("distance");

//            System.out.println("Distance: " + Distance);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
