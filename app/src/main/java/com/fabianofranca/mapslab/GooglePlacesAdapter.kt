package com.fabianofranca.mapslab

import android.content.Context
import android.os.Handler
import android.widget.ArrayAdapter
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.*
import kotlin.concurrent.fixedRateTimer


class GooglePlacesAdapter : ArrayAdapter<String> {

    private val predictions: MutableList<AutocompletePrediction> = mutableListOf()
    private val places: PlacesClient
    var origin: LatLng? = null
    var searching: (() -> Unit)? = null

    constructor(
        context: Context,
        key: String
    ) : this(context, key, R.layout.menu_auto_complete)

    private constructor(
        context: Context,
        key: String,
        layout: Int
    ) : super(
        context,
        layout
    ) {
        if (!Places.isInitialized()) {
            Places.initialize(context.applicationContext, key)
        }

        places = Places.createClient(context)
    }

    override fun getFilter(): Filter {
        return Filter()
    }

    override fun getItem(position: Int): String? {
        return "${predictions[position].getPrimaryText(null)}\n${predictions[position].getSecondaryText(null)}"
    }

    fun getLocationByPosition(position: Int, callback: (LatLng?) -> Unit) {

        val placeId = predictions[position].placeId

        val placeFields = listOf(Place.Field.LAT_LNG)

        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        places.fetchPlace(request).addOnSuccessListener {
            callback(it.place.latLng)
        }.addOnFailureListener {}
    }

    override fun getCount() = predictions.size

    inner class Filter : android.widget.Filter() {

        private var timer: Timer? = null

        override fun performFiltering(constraint: CharSequence?): FilterResults? {
            constraint?.let { query ->
                if (query.length >= 3) {
                    timer?.cancel()

                    timer = fixedRateTimer(initialDelay = 1000, period = 1000) {
                        searching?.let {
                            Handler(context.mainLooper).post { it() }
                        }

                        cancel()

                        val builder = FindAutocompletePredictionsRequest.builder()
                            .setTypeFilter(TypeFilter.ADDRESS)
                            .setQuery(query.toString())

                        origin?.let { builder.setOrigin(it) }

                        val request = builder.build()

                        places.findAutocompletePredictions(request)
                            .addOnSuccessListener { response ->
                                this@GooglePlacesAdapter.predictions.clear()
                                this@GooglePlacesAdapter.predictions.addAll(response.autocompletePredictions)
                                this@GooglePlacesAdapter.notifyDataSetChanged()
                            }.addOnFailureListener {}
                    }
                }
            }

            return null
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) = Unit
    }
}