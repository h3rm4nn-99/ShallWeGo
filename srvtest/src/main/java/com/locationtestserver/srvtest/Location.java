package com.locationtestserver.srvtest;

import org.apache.lucene.util.SloppyMath;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.URL;

public class Location {
    private double latitude;
    private double longitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double distance(Location l) {
        return SloppyMath.haversinMeters(this.latitude, this.longitude, l.getLatitude(), l.getLongitude()) / 1000;
    }

    public double distance(String comune) throws IOException, ParseException {
        URL nominatimServer = new URL("http://192.168.1.22/nominatim/search.php?q=" + comune.replace(" ", "%20"));
        HttpURLConnection con = (HttpURLConnection) nominatimServer.openConnection();
        con.setRequestMethod("GET");

        String line = "";
        StringBuffer response = new StringBuffer();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        con.disconnect();
        String responseString = response.toString();
        JSONParser parser = new JSONParser();
        JSONArray array = (JSONArray) parser.parse(responseString);
        if (array.isEmpty()) {
            throw new IllegalStateException("Comune non trovato!");
        }

        JSONObject obj = (JSONObject) array.get(0);
        double latitude = Double.parseDouble(obj.get("lat").toString());
        double longitude = Double.parseDouble(obj.get("lon").toString());

        return SloppyMath.haversinMeters(this.latitude, this.longitude, latitude, longitude) / 1000;
    }
}
