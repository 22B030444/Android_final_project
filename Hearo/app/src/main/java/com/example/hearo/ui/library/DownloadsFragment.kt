package com.example.hearo.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hearo.R
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.databinding.FragmentDownloadsBinding
import com.example.hearo.ui.adapter.DownloadedTrackAdapter
import com.example.hearo.ui.playlist.AddToPlaylistDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DownloadsFragment : Fragment() {

    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DownloadsViewModel by viewModels()

    private var downloadedTracks: List<UniversalTrack> = emptyList()

    private val tracksAdapter by lazy {
        DownloadedTrackAdapter(
            onTrackClick = { downloadedTrack ->
                val position = downloadedTracks.indexOfFirst { it.id == downloadedTrack.track.id }
                val bundle = bundleOf(
                    "track" to downloadedTrack.track,
                    "trackList" to ArrayList(downloadedTracks),
                    "currentIndex" to position
                )
                findNavController().navigate(R.id.action_downloadsFragment_to_playerFragment, bundle)
            },
            onDeleteClick = { downloadedTrack ->
                showDeleteDialog(downloadedTrack)
            },
            onLongClick = { downloadedTrack ->
                showTrackOptionsDialog(downloadedTrack)
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupSearch()
        setupRecyclerView()
        observeData()
    }

    private fun setupToolbar() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.clearAllButton.setOnClickListener {
            showClearAllDialog()
        }
    }

    private fun setupSearch() {
        binding.searchEditText.doAfterTextChanged { text ->
            viewModel.searchDownloads(text?.toString() ?: "")
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tracksAdapter
        }
    }

    private fun observeData() {
        viewModel.downloads.observe(viewLifecycleOwner) { downloads ->
            downloadedTracks = downloads.map { it.track }

            if (downloads.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                binding.storageInfoLayout.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                binding.storageInfoLayout.visibility = View.VISIBLE
                tracksAdapter.submitList(downloads)
            }
        }

        viewModel.storageInfo.observe(viewLifecycleOwner) { info ->
            binding.storageInfoText.text = info
        }
    }

    private fun showDeleteDialog(downloadedTrack: com.example.hearo.data.repository.DownloadedTrack) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Download")
            .setMessage("Remove \"${downloadedTrack.track.name}\" from downloads?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteDownload(downloadedTrack.track.id)
                Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearAllDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear All Downloads")
            .setMessage("This will delete all downloaded tracks. Are you sure?")
            .setPositiveButton("Clear All") { _, _ ->
                viewModel.clearAllDownloads()
                Toast.makeText(requireContext(), "All downloads cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTrackOptionsDialog(downloadedTrack: com.example.hearo.data.repository.DownloadedTrack) {
        val options = arrayOf("Play", "Add to playlist", "Delete")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(downloadedTrack.track.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val position = downloadedTracks.indexOfFirst { it.id == downloadedTrack.track.id }
                        val bundle = bundleOf(
                            "track" to downloadedTrack.track,
                            "trackList" to ArrayList(downloadedTracks),
                            "currentIndex" to position
                        )
                        findNavController().navigate(R.id.action_downloadsFragment_to_playerFragment, bundle)
                    }
                    1 -> {
                        val dialog = AddToPlaylistDialog.newInstance(downloadedTrack.track)
                        dialog.show(parentFragmentManager, "AddToPlaylistDialog")
                    }
                    2 -> {
                        showDeleteDialog(downloadedTrack)
                    }
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


