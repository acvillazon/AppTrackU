package com.example.webservicetracku.database.core;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.example.webservicetracku.database.daos.GeolocationDao;
import com.example.webservicetracku.database.daos.UserDao;
import com.example.webservicetracku.database.entities.Geolocation;
import com.example.webservicetracku.database.entities.User;


@Database(entities = {User.class, Geolocation.class},version = 11)
public abstract class TrackUDatabaseManager extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract GeolocationDao geoDao();
}