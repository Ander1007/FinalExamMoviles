// TopPlace.kt
package cr.ac.una.wikipedia.entity

data class TopPlace(
    val placeName: String,
    val thumbnailUrl: String?,
    val description: String,
    val visitCount: Int
)
