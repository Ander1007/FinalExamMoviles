// TopPlacesAdapter.kt
package cr.ac.una.wikipedia.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import cr.ac.una.wikipedia.R
import cr.ac.una.wikipedia.entity.TopPlace

class TopPlacesAdapter(private val topPlacesList: List<TopPlace>) :
    RecyclerView.Adapter<TopPlacesAdapter.TopPlacesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopPlacesViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_top_places, parent, false)
        return TopPlacesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TopPlacesViewHolder, position: Int) {
        val topPlace = topPlacesList[position]
        holder.textViewPlaceName.text = topPlace.placeName
        holder.textViewDescription.text = topPlace.description
        holder.textViewVisitCount.text = "Visits: ${topPlace.visitCount}"
        Glide.with(holder.itemView.context)
            .load(topPlace.thumbnailUrl)
            .into(holder.imageViewThumbnail)
    }

    override fun getItemCount() = topPlacesList.size

    class TopPlacesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewPlaceName: TextView = itemView.findViewById(R.id.textViewPlaceName)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        val textViewVisitCount: TextView = itemView.findViewById(R.id.textViewVisitCount)
        val imageViewThumbnail: ImageView = itemView.findViewById(R.id.imageViewThumbnail)
    }
}
