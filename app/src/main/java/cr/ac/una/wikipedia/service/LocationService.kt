package cr.ac.una.wikipedia.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.libraries.places.api.Places
import cr.ac.una.wikipedia.MainActivity
import cr.ac.una.wikipedia.R
import cr.ac.una.wikipedia.db.AppDatabase
import cr.ac.una.wikipedia.entity.PlaceEntity
import cr.ac.una.wikipedia.entity.WikipediaArticle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.abs

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var notificationManager: NotificationManager
    private var contNotificacion = 2
    private var lastLatitude = 0.0
    private var lastLongitude = 0.0
    private val LOCATION_DIFFERENCE_THRESHOLD = 0.01


    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Places.initialize(applicationContext, "AIzaSyBLiFVeg7U_Ugu5bMf7EQ_TBEfPE3vOSF4")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()
        startForeground(1, createNotification("Service running"))

        requestLocationUpdates()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            "locationServiceChannel",
            "Location Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(serviceChannel)
    }

    private fun createNotification(message: String): Notification {
        return NotificationCompat.Builder(this, "locationServiceChannel")
            .setContentTitle("Location Service")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10000
        ).apply {
            setMinUpdateIntervalMillis(5000)
        }.build()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location ->
                if (isSignificantLocationChange(location.latitude, location.longitude)) {
                    getPlaceName(location.latitude, location.longitude)
                }
            }
        }
    }

    private fun isSignificantLocationChange(latitude: Double, longitude: Double): Boolean {
        val latitudeDifference = abs(latitude - lastLatitude)
        val longitudeDifference = abs(longitude - lastLongitude)
        return if (latitudeDifference > LOCATION_DIFFERENCE_THRESHOLD || longitudeDifference > LOCATION_DIFFERENCE_THRESHOLD) {
            lastLatitude = latitude
            lastLongitude = longitude
            true
        } else {
            false
        }
    }

    private suspend fun thumbnailUrl(placeName: String): WikipediaArticle? {
        val url = "https://en.wikipedia.org/api/rest_v1/page/summary/$placeName"
        return try {
            val apiResponse = URL(url).readText()
            val jsonObject = JSONObject(apiResponse)
            val article = WikipediaArticle(
                title = jsonObject.getString("title"),
                thumbnailUrl = jsonObject.optJSONObject("thumbnail")?.optString("source"),
                url = jsonObject.getString("content_urls").let {
                    JSONObject(it).getJSONObject("desktop").getString("page")
                },
                description = jsonObject.optString("extract")
            )
            article
        } catch (e: Exception) {
            Log.e("LocationService", "Error obteniendo el título de Wikipedia", e)
            null
        }
    }

    private fun getPlaceName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty() && addresses[0].locality != null) {
                val cityName = addresses[0].locality

                // Formatear la fecha
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentDateAndTime: String = sdf.format(Date())

                // Obtener el título de forma asíncrona
                CoroutineScope(Dispatchers.IO).launch {
                    val wikiArticle = thumbnailUrl(cityName)

                    // Crear el objeto PlaceEntity
                    wikiArticle?.let { article ->
                        val placeEntity = PlaceEntity(
                            id = null,
                            latitude = latitude,
                            longitude = longitude,
                            detectedAt = currentDateAndTime,
                            wikipediaArticleTitle = article.title,
                            placeName = cityName,
                            thumbnailUrl = getThumbnailUrl(article.thumbnailUrl),
                            description = article.description,
                        )

                        insertPlace(placeEntity)

                        // Enviar notificación y realizar otras acciones necesarias en el hilo principal
                        CoroutineScope(Dispatchers.Main).launch {
                            sendNotification("Ubicación actual: $cityName (Lat: $latitude, Long: $longitude)", cityName)
                            fetchRelatedWikipediaContent(cityName)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LocationService", "Error obteniendo el nombre del lugar", e)
            sendNotification("Ubicación: Latitud: $latitude, Longitud: $longitude", null)
        }
    }

    private fun getThumbnailUrl(thumbnailUrl: String?): String {
        // Si el thumbnailUrl es null o está vacío, regresamos una URL genérica
        return thumbnailUrl ?: "https://upload.wikimedia.org/wikipedia/commons/8/86/Man_o%27war_cove_near_lulworth_dorset_arp.jpg"
    }

    private suspend fun insertPlace(placeEntity: PlaceEntity) {
        // Obtener instancia de la base de datos
        val database = AppDatabase.getInstance(applicationContext)

        // Insertar en la base de datos usando el DAO
        database.placeDao().insert(placeEntity)
    }

    private fun fetchRelatedWikipediaContent(placeName: String) {
        val url = "https://en.wikipedia.org/api/rest_v1/page/related/${placeName.replace(" ", "_")}"
        Executors.newSingleThreadExecutor().execute {
            try {
                val apiResponse = URL(url).readText()
                val jsonObject = JSONObject(apiResponse)
                val pages = jsonObject.getJSONArray("pages")

                if (pages.length() > 0) {
                    val relatedArticles = mutableListOf<String>()
                    for (i in 0 until pages.length()) {
                        val page = pages.getJSONObject(i)
                        val title = page.getString("title")
                        relatedArticles.add(title)
                    }
                    if (relatedArticles.isNotEmpty()) {
                        sendNotification("Contenidos relacionados en Wikipedia: ${relatedArticles.joinToString(", ")}", placeName)
                    }
                }
            } catch (e: Exception) {
                Log.e("LocationService", "Error obteniendo contenido relacionado de Wikipedia", e)
            }
        }
    }

    private fun sendNotification(message: String, placeName: String?) {
        contNotificacion++

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("place_name", placeName)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "locationServiceChannel")
            .setContentTitle("Notificación de Servicio de Ubicación")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .addAction(R.mipmap.ic_launcher, "Mostrar", pendingIntent)
            .build()

        notificationManager.notify(contNotificacion, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
