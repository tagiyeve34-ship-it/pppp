package com.ailenezareti.panelapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ailenezareti.panelapp.api.ApiClient
import com.ailenezareti.panelapp.databinding.FragmentAlertsBinding
import com.ailenezareti.panelapp.model.MarkReadRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlertsFragment : Fragment(), Refreshable {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.alertsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.swipeRefresh.setOnRefreshListener { refresh() }
        refresh()
    }

    override fun refresh() {
        val activity = activity as? MainActivity ?: return
        val child = activity.activeChild() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.get(requireContext()).getAlerts(child.id)
                requireActivity().runOnUiThread {
                    _binding ?: return@runOnUiThread
                    binding.swipeRefresh.isRefreshing = false
                    val alerts = (response.body()?.alerts ?: emptyList()).toMutableList()
                    binding.alertsRecycler.adapter = AlertsAdapter(alerts) { alert ->
                        markRead(alert.id)
                    }
                    binding.alertsEmptyText.visibility = if (alerts.isEmpty()) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    _binding ?: return@runOnUiThread
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun markRead(alertId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiClient.get(requireContext()).markAlertRead(MarkReadRequest(alertId))
            } catch (e: Exception) { /* növbəti yenilənmədə düzələcək */ }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
