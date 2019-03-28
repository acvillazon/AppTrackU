package com.example.webservicetracku.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;


import com.example.webservicetracku.database.entities.Geolocation;

import java.util.List;

@Dao
public interface GeolocationDao {

    @Query("select * from geolocation")
    List<Geolocation> getAllGeolocation();

    @Query("SELECT * FROM Geolocation WHERE id IN (SELECT MAX(id) FROM Geolocation GROUP BY email)")
    List<Geolocation> getLastLocation();

    @Query("select * from geolocation where id=:id")
    List<Geolocation> getUserById(int id);

    @Query("select * from geolocation where email=:email")
    List<Geolocation> getUserByEmail(String email);

    @Query("select * from geolocation where (email=:email) AND (time BETWEEN :unix1 and :unix2) ")
    List<Geolocation> getHistory(String email, long unix1, long unix2);

    @Insert
    void insertGeolocation(Geolocation geolocation);

    @Delete
    void deleteGeolocation(Geolocation geolocation);
}
