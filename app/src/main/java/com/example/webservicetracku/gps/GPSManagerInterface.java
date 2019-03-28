package com.example.webservicetracku.gps;

public interface GPSManagerInterface {
    void LocationReceived(double latitude, double longitued);
    void GPSManagerException(Exception error);
}
