package cr.ac.una.wikipedia.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import cr.ac.una.wikipedia.R

class SettingsFragment : Fragment() {

    private lateinit var editTextNumber: EditText
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        editTextNumber = view.findViewById(R.id.editTextNumber)
        sharedPreferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)

        loadSettings()

        return view
    }

    private fun loadSettings() {
        val defaultValue = resources.getInteger(R.integer.default_number_of_places)
        val numberOfPlaces = sharedPreferences.getInt("numberOfPlaces", defaultValue)
        editTextNumber.setText(numberOfPlaces.toString())
    }

    override fun onStop() {
        super.onStop()
        saveSettings()
    }

    private fun saveSettings() {
        val numberOfPlaces = editTextNumber.text.toString().toIntOrNull() ?: return
        with(sharedPreferences.edit()) {
            putInt("numberOfPlaces", numberOfPlaces)
            apply()
        }
    }
}
