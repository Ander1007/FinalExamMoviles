// TopPlacesFragment.kt
package cr.ac.una.wikipedia.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cr.ac.una.wikipedia.R
import cr.ac.una.wikipedia.adapters.TopPlacesAdapter
import cr.ac.una.wikipedia.db.AppDatabase
import kotlinx.coroutines.launch

class TopPlacesFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_top_places, container, false)

        sharedPreferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)

        setupList()

        return rootView
    }

    private fun setupList() {
        val numberOfPlaces = getNumberOfPlacesFromSettings()
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerViewTopPlaces)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            val topPlaces = db.placeDao().getTopPlaces(numberOfPlaces)
            recyclerView.adapter = TopPlacesAdapter(topPlaces)
        }
    }

    private fun getNumberOfPlacesFromSettings(): Int {
        val defaultValue = resources.getInteger(R.integer.default_number_of_places)
        return sharedPreferences.getInt("numberOfPlaces", defaultValue)
    }
}
