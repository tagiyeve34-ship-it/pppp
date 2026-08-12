package com.ailenezareti.panelapp.ui

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ailenezareti.panelapp.R
import com.ailenezareti.panelapp.api.ApiClient
import com.ailenezareti.panelapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(), Refreshable {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefresh.setOnRefreshListener { refresh() }
        refresh()
    }

    override fun refresh() {
        val activity = activity as? MainActivity ?: return
        val child = activity.activeChild() ?: run {
            binding.swipeRefresh.isRefreshing = false
            return
        }

        binding.lastSeenTime.text = child.name
        val online = isOnline(child.last_seen)
        binding.statusText.text = getString(if (online) R.string.online_now else R.string.offline_now)
        (binding.statusDot.background as? android.graphics.drawable.GradientDrawable)?.setColor(
            ContextCompat.getColor(requireContext(), if (online) R.color.teal else R.color.slate)
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = ApiClient.get(requireContext())
                val locResp = api.getLocations(child.id, "today")
                val alertsResp = api.getAlerts(child.id)
                val callsResp = api.getCalls(child.id)

                requireActivity().runOnUiThread {
                    _binding ?: return@runOnUiThread
                    binding.swipeRefresh.isRefreshing = false

                    val locations = locResp.body()?.locations ?: emptyList()
                    val alerts = alertsResp.body()?.alerts ?: emptyList()
                    val calls = callsResp.body()?.calls ?: emptyList()

                    binding.todayCountText.text = locations.size.toString()
                    binding.unreadAlertsText.text = alerts.count { it.is_read == 0 }.toString()

                    val latest = locations.firstOrNull()
                    binding.lastSeenSub.text = if (latest != null) {
                        "Son koordinat: ${latest.latitude.take(7)}, ${latest.longitude.take(7)}" +
                            (latest.battery_pct?.let { " · batareya $it%" } ?: "")
                    } else {
                        getString(R.string.no_data)
                    }

                    val feed = mutableListOf<ActivityItem>()
                    latest?.let {
                        feed.add(ActivityItem(R.drawable.ic_map, "Yeni mövqe qeydə alındı", fmtTime(it.recorded_at)))
                    }
                    alerts.take(3).forEach {
                        feed.add(ActivityItem(R.drawable.ic_bell, it.message, fmtTime(it.created_at), R.color.amber, R.color.amberSoft))
                    }
                    calls.take(2).forEach {
                        val label = (it.contact_name ?: it.phone_number) + " · " + it.call_type
                        val isMissed = it.call_type == "missed"
                        feed.add(ActivityItem(
                            R.drawable.ic_phone, label, fmtTime(it.occurred_at),
                            if (isMissed) R.color.red else R.color.teal,
                            if (isMissed) R.color.redSoft else R.color.tealSoft
                        ))
                    }

                    binding.activityRecycler.layoutManager = LinearLayoutManager(requireContext())
                    binding.activityRecycler.adapter = ActivityAdapter(feed)
                    binding.homeEmptyText.visibility = if (feed.isEmpty()) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    _binding ?: return@runOnUiThread
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun isOnline(lastSeen: String?): Boolean {
        if (lastSeen == null) return false
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(lastSeen)
            (System.currentTimeMillis() - (date?.time ?: 0)) < 5 * 60_000
        } catch (e: Exception) { false }
    }

    private fun fmtTime(iso: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(iso)
            DateUtils.getRelativeTimeSpanString(date?.time ?: 0, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
        } catch (e: Exception) { iso }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
