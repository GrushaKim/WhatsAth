package com.example.mywhatsath.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import com.example.mywhatsath.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowForGoogleMap(context: Context): GoogleMap.InfoWindowAdapter {

    var mWindow = (context as Activity).layoutInflater.inflate(R.layout.item_map_marker, null)

    private fun renderWindowText(marker: Marker, view: View){
        val titleTv = view.findViewById<TextView>(R.id.titleTv)
        val snippetTv = view.findViewById<TextView>(R.id.snippetTv)

        titleTv.text = marker.title
        snippetTv.text = marker.snippet

    }

    override fun getInfoContents(marker: Marker): View {
        renderWindowText(marker, mWindow)
        return mWindow
    }

    override fun getInfoWindow(marker: Marker): View {
        renderWindowText(marker, mWindow)
        return mWindow
    }

}