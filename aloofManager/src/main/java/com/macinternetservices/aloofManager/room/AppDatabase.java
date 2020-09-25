package com.macinternetservices.aloofManager.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.macinternetservices.aloofManager.model.GeoLoc;


@Database(entities = {GeoLoc.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract GeoLocDao dataDao();
}
