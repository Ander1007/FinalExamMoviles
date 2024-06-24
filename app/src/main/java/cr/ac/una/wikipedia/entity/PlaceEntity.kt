// PlaceEntity.kt
package cr.ac.una.wikipedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val latitude: Double,
    val longitude: Double,
    val detectedAt: String,
    val wikipediaArticleTitle: String,
    val placeName: String,
    val thumbnailUrl: String?,
    val description: String
) : Serializable
