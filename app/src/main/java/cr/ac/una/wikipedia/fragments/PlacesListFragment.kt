package cr.ac.una.wikipedia.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import cr.ac.una.wikipedia.adapters.PlaceAdapter
import cr.ac.una.wikipedia.R
import cr.ac.una.wikipedia.db.AppDatabase
import cr.ac.una.wikipedia.entity.PlaceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlacesListFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var placeAdapter: PlaceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_places_list, container, false)
        listView = view.findViewById(R.id.list_view_places)
        placeAdapter = PlaceAdapter(requireContext(), emptyList())
        listView.adapter = placeAdapter

        listView.setOnItemClickListener { parent, view, position, id ->
            val selectedPlace = placeAdapter.getItem(position) as PlaceEntity
            navigateToArticleDetail(selectedPlace.wikipediaArticleTitle)
        }

        loadPlaces()

        return view
    }

    private fun loadPlaces() {
        CoroutineScope(Dispatchers.IO).launch {
            val places = AppDatabase.getInstance(requireContext()).placeDao().getAllPlaces()
            withContext(Dispatchers.Main) {
                if (places != null) {
                    placeAdapter.updatePlaces(places)
                }
            }
        }
    }

    private fun navigateToArticleDetail(articleTitle: String) {
        val fragment = ArticleDetailFragment()
        val bundle = Bundle()
        bundle.putString("article_url", "https://en.wikipedia.org/wiki/$articleTitle")
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }
}
