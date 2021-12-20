package com.example.mywhatsath.models

import android.content.Context
import androidx.room.*

@Entity
data class ModelPlace(
    @PrimaryKey
    var id: String,
    var name: String,
    var address: String,
    var latitude: Double,
    var longitude: Double,
    var rating: Double
)

@Dao
interface PlaceDao {

    @Query("SELECT * FROM ModelPlace")
    fun getAll(): List<ModelPlace>

    @Insert
    fun insertPlace(place: ModelPlace)

    @Update
    fun updatePlace(place: ModelPlace)

    @Delete
    fun deletePlace(place: ModelPlace)
}

@Database(entities = [ModelPlace::class], version = 1)
abstract class PlaceDatabase: RoomDatabase(){
    abstract fun placeDao(): PlaceDao

    companion object{
        private var instance: PlaceDatabase? = null

        @Synchronized
        fun getInstance(context: Context): PlaceDatabase? {
            if(instance == null) {
                synchronized(PlaceDatabase::class){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        PlaceDatabase::class.java,
                        "place-database"
                    ).build()
                }
            }
            return instance
        }
    }
}