package com.ailenezareti.panelapp.ui

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ailenezareti.panelapp.BuildConfig
import com.ailenezareti.panelapp.api.ApiClient
import com.ailenezareti.panelapp.databinding.FragmentLocationBinding
import com.ailenezareti.panelapp.model.LocationPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class LocationFragment : Fragment(), Refreshable {

    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!
    private var currentRange = "3h"
    private var lastPoints: List<GeoPoint> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Configuration.getInstance().load(
            requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext())
        )
        Configuration.getInstance().userAgentValue = requireContext().packageName
        // Xarici yaddaş icazəsi tələb olunmasın deyə tətbiqin öz keş qovluğundan istifadə edirik
        Configuration.getInstance().osmdroidBasePath = requireContext().cacheDir
        Configuration.getInstance().osmdroidTileCache = java.io.File(requireContext().cacheDir, "osmdroid_tiles")
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        binding.mapView.controller.setZoom(14.0)

        val ranges = listOf(
            "3h" to "3 saat", "6h" to "6 saat", "today" to "Bugün",
            "3d" to "3 gün", "7d" to "7 gün", "all" to "Hamısı"
        )
        binding.rangeRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rangeRecycler.adapter = RangeAdapter(ranges, currentRange) { key ->
            currentRange = key
            loadLocations()
        }

        binding.recenterFab.setOnClickListener { recenter() }

        refresh()
    }

    override fun refresh() {
        loadLocations()
    }

    private fun loadLocations() {
        val activity = activity as? MainActivity ?: return
        val child = activity.activeChild() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.get(requireContext()).getLocations(child.id, currentRange)
                requireActivity().runOnUiThread {
                    _binding ?: return@runOnUiThread
                    val points = response.body()?.locations ?: emptyList()
                    drawRoute(points)
                }
            } catch (e: Exception) {
                // sükutla keç, xəritə boş qalır
            }
        }
    }

    private fun drawRoute(points: List<LocationPoint>) {
        binding.mapView.overlays.clear()
        binding.locationEmptyText.visibility = if (points.isEmpty()) View.VISIBLE else View.GONE
        binding.lastUpdateText.text = if (points.isEmpty()) "Məlumat yoxdur" else formatRecordedAt(points.first().recorded_at)
        if (points.isEmpty()) {
            binding.mapView.invalidate()
            return
        }

        // API son-tarixdən köhnəyə doğru qaytarır — marşrut üçün tərsinə çeviririk
        val chronological = points.reversed()
        val geoPoints = chronological.map { GeoPoint(it.latitude.toDouble(), it.longitude.toDouble()) }
        lastPoints = geoPoints

        val polyline = Polyline().apply {
            setPoints(geoPoints)
            outlinePaint.color = Color.parseColor("#2E6F6B")
            outlinePaint.strokeWidth = 6f
            outlinePaint.alpha = 160
        }
        binding.mapView.overlays.add(polyline)

        geoPoints.forEachIndexed { index, geoPoint ->
            val marker = Marker(binding.mapView)
            marker.position = geoPoint
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            if (index == geoPoints.size - 1) {
                marker.title = "Son mövqe"
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            binding.mapView.overlays.add(marker)
        }

        binding.mapView.invalidate()
        recenter()
    }

    private fun formatRecordedAt(value: String): String {
        return value
            .replace("T", " ")
            .replace("Z", "")
            .substringBefore("+")
            .take(19)
            .ifBlank { value }
    }

    private fun recenter() {
        if (lastPoints.isEmpty()) return
        if (lastPoints.size == 1) {
            binding.mapView.controller.setCenter(lastPoints.first())
            binding.mapView.controller.setZoom(16.0)
        } else {
            val box = org.osmdroid.util.BoundingBox.fromGeoPoints(lastPoints)
            binding.mapView.post {
                binding.mapView.zoomToBoundingBox(box, false, 80)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
