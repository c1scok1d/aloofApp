package com.macinternetservices.aloofManager.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.macinternetservices.aloofManager.model.GeoLoc;

import java.util.List;

@Dao
public interface GeoLocDao {

    @Query("SELECT * FROM GeoLoc")
    List<GeoLoc> getAll();

    @Query("DELETE FROM GeoLoc")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GeoLoc data);

    @Delete
    void delete(GeoLoc data);

    @Update
    void update(GeoLoc data);

}
