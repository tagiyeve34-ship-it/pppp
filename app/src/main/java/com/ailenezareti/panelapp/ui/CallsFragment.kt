package com.ailenezareti.panelapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ailenezareti.panelapp.api.ApiClient
import com.ailenezareti.panelapp.databinding.FragmentCallsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallsFragment : Fragment(), Refreshable {

    private var _binding: FragmentCallsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCallsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.callsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.swipeRefresh.setOnRefreshListener { refresh() }
        refresh()
    }

    override fun refresh() {
        val activity = activity as? MainActivity ?: return
        val child = activity.activeChild() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.get(requireContext()).getCalls(child.id)
                requireActivity().runOnUiThread {
                    _binding ?: return@runOnUiThread
                    binding.swipeRefresh.isRefreshing = false
                    val calls = response.body()?.calls ?: emptyList()
                    binding.callsRecycler.adapter = CallsAdapter(calls)
                    binding.callsEmptyText.visibility = if (calls.isEmpty()) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    _binding ?: return@runOnUiThread
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
