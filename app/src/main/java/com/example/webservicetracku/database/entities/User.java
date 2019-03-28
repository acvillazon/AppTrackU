package com.example.webservicetracku.database.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import androidx.annotation.NonNull;

@Entity(indices = {@Index(value = {"email"},
        unique = true)})

public class User {

    @PrimaryKey(autoGenerate = true)
    public int userId;

    @NonNull
    @ColumnInfo(name="email")
    public String email;

    @NonNull
    @ColumnInfo(name="password_hash")
    public String passwordHash;
}
