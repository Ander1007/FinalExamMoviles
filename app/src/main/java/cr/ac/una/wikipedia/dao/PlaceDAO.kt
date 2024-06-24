package cr.ac.una.wikipedia.dao

import cr.ac.una.wikipedia.entity.PlaceEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cr.ac.una.wikipedia.entity.TopPlace

@Dao
interface PlaceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(place: PlaceEntity)

    @Query("""
        SELECT placeName, thumbnailUrl, description, COUNT(placeName) as visitCount 
        FROM places 
        GROUP BY placeName 
        ORDER BY visitCount DESC 
        LIMIT :limit
    """)
    suspend fun getTopPlaces(limit: Int): List<TopPlace>

    @Query("SELECT * FROM places")
    suspend fun getAllPlaces(): List<PlaceEntity>?
}


