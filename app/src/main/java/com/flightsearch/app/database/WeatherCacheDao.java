package com.flightsearch.app.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface WeatherCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WeatherCacheEntity entity);

    @Query("SELECT * FROM weather_cache WHERE cache_key = :key LIMIT 1")
    WeatherCacheEntity getByKey(String key);

    @Query("DELETE FROM weather_cache WHERE cached_at < :expiryTime")
    void deleteExpired(long expiryTime);
}
