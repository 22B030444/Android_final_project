package com.example.hearo.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hearo.data.model.UniversalTrack
import com.example.hearo.databinding.DialogAddToPlaylistBinding
import com.example.hearo.ui.adapter.PlaylistSelectAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddToPlaylistDialog : BottomSheetDialogFragment() {

    private var _binding: DialogAddToPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddToPlaylistViewModel by viewModels()

    private var track: UniversalTrack? = null

    private val playlistAdapter by lazy {
        PlaylistSelectAdapter { playlist ->
            track?.let { t ->
                viewModel.addTrackToPlaylist(playlist.playlist.id, t)
                Toast.makeText(
                    requireContext(),
                    "Added to ${playlist.playlist.name}",
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddToPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        track = arguments?.getParcelable("track")

        setupRecyclerView()
        setupClickListeners()
        observePlaylists()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = playlistAdapter
        }
    }

    private fun setupClickListeners() {
        binding.createNewPlaylistButton.setOnClickListener {
            showCreatePlaylistDialog()
        }

        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun observePlaylists() {
        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            if (playlists.isEmpty()) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyStateText.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                playlistAdapter.submitList(playlists)
            }
        }
    }

    private fun showCreatePlaylistDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(com.example.hearo.R.layout.dialog_create_playlist, null)

        val nameInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.example.hearo.R.id.playlistNameInput
        )

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create Playlist")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val name = nameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.createPlaylistAndAddTrack(name, track)
                    Toast.makeText(requireContext(), "Created and added!", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Enter playlist name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(track: UniversalTrack): AddToPlaylistDialog {
            return AddToPlaylistDialog().apply {
                arguments = Bundle().apply {
                    putParcelable("track", track)
                }
            }
        }
    }
}


