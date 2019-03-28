package com.example.webservicetracku.database.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import androidx.annotation.NonNull;

@Entity
public class Geolocation {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name="email")
    public String email;

    @NonNull
    @ColumnInfo(name="latitude")
    public double latitude;

    @NonNull
    @ColumnInfo(name="longitude")
    public double longitude;

    @NonNull
    @ColumnInfo(name="time")
    public long time;

}
