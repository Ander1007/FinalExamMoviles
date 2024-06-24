package cr.ac.una.wikipedia.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import cr.ac.una.wikipedia.R
import cr.ac.una.wikipedia.entity.PlaceEntity

class PlaceAdapter(private val context: Context, private var places: List<PlaceEntity>) : BaseAdapter() {

    override fun getCount(): Int {
        return places.size
    }

    override fun getItem(position: Int): Any {
        return places[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_place, parent, false)

        val place = places[position]

        val placeNameTextView: TextView = view.findViewById(R.id.place_name)
        val detectedAtTextView: TextView = view.findViewById(R.id.detected_at)
        val descriptionTextView: TextView = view.findViewById(R.id.description)
        val imageView: ImageView = view.findViewById(R.id.image)

        placeNameTextView.text = place.placeName
        detectedAtTextView.text = place.detectedAt
        descriptionTextView.text = place.description

        // Utiliza Glide para cargar la imagen desde el URL del artículo de Wikipedia
        Glide.with(context)
            .load(place.thumbnailUrl) // Obtener la URL del thumbnail correcta
            .into(imageView)

        return view
    }


    fun updatePlaces(newPlaces: List<PlaceEntity>) {
        places = newPlaces
        notifyDataSetChanged()
    }
}
