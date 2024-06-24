package cr.ac.una.wikipedia.entity

data class WikipediaArticle(
    val title: String,
    val thumbnailUrl: String?,
    val url: String,
    val description: String
)

