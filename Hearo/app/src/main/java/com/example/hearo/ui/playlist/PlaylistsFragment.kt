package com.example.hearo.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hearo.R
import com.example.hearo.databinding.FragmentPlaylistsBinding
import com.example.hearo.ui.adapter.PlaylistAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistsViewModel by viewModels()

    private val playlistAdapter by lazy {
        PlaylistAdapter(
            onPlaylistClick = { playlistWithTracks ->
                val bundle = bundleOf(
                    "playlistId" to playlistWithTracks.playlist.id,
                    "playlistName" to playlistWithTracks.playlist.name
                )
                findNavController().navigate(
                    R.id.action_playlistsFragment_to_playlistDetailFragment,
                    bundle
                )
            },
            onPlaylistLongClick = { playlistWithTracks ->
                showPlaylistOptionsDialog(playlistWithTracks.playlist.id, playlistWithTracks.playlist.name)
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        observePlaylists()
    }

    private fun setupToolbar() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = playlistAdapter
        }
    }

    private fun setupFab() {
        binding.createPlaylistFab.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }

    private fun observePlaylists() {
        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            if (playlists.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                playlistAdapter.submitList(playlists)
            }
        }
    }

    private fun showCreatePlaylistDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_playlist, null)

        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.playlistNameInput)
        val descriptionInput = dialogView.findViewById<TextInputEditText>(R.id.playlistDescriptionInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create Playlist")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val name = nameInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()

                if (name.isNotEmpty()) {
                    viewModel.createPlaylist(name, description.ifEmpty { null })
                    Toast.makeText(requireContext(), "Playlist created", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPlaylistOptionsDialog(playlistId: Long, playlistName: String) {
        val options = arrayOf("Rename", "Delete")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(playlistName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showRenamePlaylistDialog(playlistId, playlistName)
                    1 -> showDeleteConfirmDialog(playlistId, playlistName)
                }
            }
            .show()
    }

    private fun showRenamePlaylistDialog(playlistId: Long, currentName: String) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_playlist, null)

        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.playlistNameInput)
        nameInput.setText(currentName)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Rename Playlist")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameInput.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.renamePlaylist(playlistId, newName)
                    Toast.makeText(requireContext(), "Playlist renamed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmDialog(playlistId: Long, playlistName: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Playlist")
            .setMessage("Are you sure you want to delete \"$playlistName\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePlaylist(playlistId)
                Toast.makeText(requireContext(), "Playlist deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


