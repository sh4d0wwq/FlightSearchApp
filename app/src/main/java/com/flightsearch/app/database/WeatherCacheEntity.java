package com.flightsearch.app.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "weather_cache", indices = {@Index(value = "cache_key", unique = true)})
public class WeatherCacheEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "cache_key")
    public String cacheKey;

    @ColumnInfo(name = "weather_json")
    public String weatherJson;

    @ColumnInfo(name = "cached_at")
    public long cachedAt;

    public WeatherCacheEntity(String cacheKey, String weatherJson, long cachedAt) {
        this.cacheKey = cacheKey;
        this.weatherJson = weatherJson;
        this.cachedAt = cachedAt;
    }
}
