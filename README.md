# Hearo Android App

## Team Members

| Student Name          | Student ID   |
|-----------------------|--------------|
| Suleimenova Zhasmin   | 22B030444    |
| Bexeit Alua           | 22B030283    |

## Table of Contents

- [Project Overview](#project-overview)
- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Requirements](#requirements)
- [Installation](#installation)
- [API Integration](#api-integration)
- [Project Structure](#project-structure)
- [Screenshots](#screenshots)

## Project Overview

**Hearo** is a modern Kotlin-based Android music streaming app built using Android Studio. The app allows users to search, discover, and play music from multiple sources including iTunes and Jamendo APIs. With features like offline downloads, playlist management, and a persistent mini-player, Hearo provides a seamless music listening experience while adhering to modern Android development principles using MVVM (Model-View-ViewModel) architecture.

## Key Features

- **Multi-Source Music Search**: Search tracks, albums, and artists from iTunes and Jamendo APIs
- **Music Playback**: Play 30-second previews (iTunes) or full tracks (Jamendo) with play/pause, skip, shuffle, and repeat controls
- **Mini Player**: Persistent mini-player that continues playback while navigating between screens
- **Playlist Management**: Create, edit, rename, and delete custom playlists
- **Liked Songs**: Save favorite tracks to a dedicated liked songs collection
- **Artist Following**: Follow artists and view their discographies
- **Album Browsing**: Explore albums and view track listings
- **Download Manager**: Download tracks for offline listening with progress tracking
- **Listening History**: Track recently played songs with automatic history management
- **User Profile**: View personal statistics including liked songs, playlists, and followed artists
- **Dark Theme**: Beautiful purple gradient dark theme throughout the app
- **Smooth Navigation**: Bottom navigation with seamless transitions between Home, Search, and Library

## Tech Stack

| Technology | Purpose |
|------------|---------|
| **Kotlin** | Primary programming language |
| **Android Jetpack** | Navigation, LiveData, ViewModel, Room |
| **Room Database** | Local data persistence |
| **Retrofit** | REST API communication |
| **OkHttp** | HTTP client for downloads |
| **Glide** | Image loading and caching |
| **Coroutines & Flow** | Asynchronous programming |
| **Material Design 3** | UI components and theming |

## Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture pattern:
```
┌─────────────────────────────────────────────────────────┐
│                        UI Layer                          │
│  (Fragments, Activities, Adapters, Custom Views)        │
└─────────────────────────┬───────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────┐
│                    ViewModel Layer                       │
│  (LiveData, StateFlow, UI State Management)             │
└─────────────────────────┬───────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────┐
│                   Repository Layer                       │
│  (MusicRepository, PlaylistRepository, etc.)            │
└──────────┬──────────────────────────────┬───────────────┘
           │                              │
┌──────────▼──────────┐      ┌───────────▼────────────────┐
│    Remote Source    │      │       Local Source         │
│  (iTunes, Jamendo)  │      │  (Room Database, Prefs)    │
└─────────────────────┘      └────────────────────────────┘
```

## Requirements

- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: Version 17 or higher
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)
- **Kotlin**: 1.9.0+

## Installation

### Clone the Repository
```bash
git clone https://github.com/22B030444/Android_final_project/
cd Android_final_project
```

### Open in Android Studio

1. Open Android Studio
2. Select "Open an existing Android Studio project"
3. Navigate to the cloned directory and select it
4. Wait for Gradle sync to complete

### Build and Run

1. Connect an Android device or start an emulator
2. Click "Run" (▶️) or press `Shift + F10`
3. Select your target device

## API Integration

### iTunes Search API
- **Base URL**: `https://itunes.apple.com/`
- **Features**: Search tracks, albums, artists; Get artist details and discography
- **Limitations**: 30-second preview only

### Jamendo API
- **Base URL**: `https://api.jamendo.com/v3.0/`
- **Features**: Search royalty-free music, full track streaming and download
- **Client ID**: Required (configured in Constants.kt)

## Project Structure
```
app/src/main/java/com/example/hearo/
│
├── 📂 data/
│   ├── 📂 api/
│   │   ├── ITunesApiService.kt        
│   │   ├── ITunesRetrofitClient.kt    
│   │   ├── JamendoApiService.kt     
│   │   └── JamendoRetrofitClient.kt   
│   │
│   ├── 📂 database/
│   │   ├── MusicDatabase.kt           
│   │   ├── 📂 dao/
│   │   │   ├── TrackDao.kt            
│   │   │   ├── PlaylistDao.kt
│   │   │   ├── ArtistDao.kt            
│   │   │   ├── AlbumDao.kt             
│   │   │   ├── RecentlyPlayedDao.kt    
│   │   │   └── DownloadedTrackDao.kt  
│   │   └── 📂 entity/
│   │       ├── TrackEntity.kt        
│   │       ├── PlaylistEntity.kt      
│   │       ├── PlaylistTrackEntity.kt  
│   │       ├── PlaylistWithTracks.kt 
│   │       ├── ArtistEntity.kt        
│   │       ├── AlbumEntity.kt          
│   │       ├── RecentlyPlayedEntity.kt 
│   │       └── DownloadedTrackEntity.kt
│   │
│   ├── 📂 model/
│   │   ├── UniversalTrack.kt          
│   │   ├── UniversalArtist.kt         
│   │   ├── UniversalAlbum.kt          
│   │   ├── UiState.kt                
│   │   ├── 📂 itunes/
│   │   │   └── ITunesModels.kt       
│   │   └── 📂 jamendo/
│   │       └── JamendoModels.kt      
│   │
│   ├── 📂 repository/
│   │   ├── MusicRepository.kt          
│   │   ├── PlaylistRepository.kt    
│   │   ├── HistoryRepository.kt        
│   │   └── DownloadsRepository.kt  
│   │
│   └── 📂 preferences/
│       └── AppPreferences.kt          
│
├── 📂 service/
│   └── MusicPlayerService.kt        
│
├── 📂 ui/
│   ├── 📂 home/
│   │   ├── HomeFragment.kt             
│   │   └── HomeViewModel.kt           
│   │
│   ├── 📂 search/
│   │   ├── SearchFragment.kt          
│   │   └── SearchViewModel.kt        
│   │
│   ├── 📂 library/
│   │   ├── LibraryFragment.kt         
│   │   ├── LibraryViewModel.kt       
│   │   ├── LikedSongsFragment.kt      
│   │   ├── LikedSongsViewModel.kt    
│   │   ├── PlaylistsFragment.kt       
│   │   ├── ArtistsFragment.kt        
│   │   ├── ArtistsViewModel.kt      
│   │   ├── DownloadsFragment.kt       
│   │   └── DownloadsViewModel.kt    
│   │
│   ├── 📂 player/
│   │   ├── PlayerFragment.kt          
│   │   ├── PlayerViewModel.kt          
│   │   ├── MiniPlayerView.kt          
│   │   └── MediaPlayerManager.kt       
│   │
│   ├── 📂 artist/
│   │   ├── ArtistDetailFragment.kt    
│   │   └── ArtistDetailViewModel.kt    
│   │
│   ├── 📂 playlist/
│   │   ├── PlaylistsFragment.kt       
│   │   ├── PlaylistsViewModel.kt    
│   │   ├── PlaylistDetailFragment.kt 
│   │   ├── PlaylistDetailViewModel.kt  
│   │   ├── AddToPlaylistDialog.kt     
│   │   └── AddToPlaylistViewModel.kt  
│   │
│   ├── 📂 profile/
│   │   ├── ProfileFragment.kt         
│   │   └── ProfileViewModel.kt        
│   │
│   ├── 📂 history/
│   │   ├── HistoryFragment.kt         
│   │   └── HistoryViewModel.kt       
│   │
│   ├── 📂 auth/
│   │   └── LoginFragment.kt           
│   │
│   └── 📂 adapter/
│       ├── UniversalTrackAdapter.kt   
│       ├── UniversalArtistAdapter.kt
│       ├── UniversalAlbumAdapter.kt 
│       ├── PlaylistAdapter.kt       
│       ├── PlaylistSelectAdapter.kt    
│       ├── ArtistGridAdapter.kt       
│       ├── HorizontalTrackAdapter.kt 
│       ├── RecentTrackAdapter.kt      
│       └── DownloadedTrackAdapter.kt   
│
├── 📂 utils/
│   ├── Constants.kt                   
│   ├── TrackDownloadManager.kt      
│   └── DownloadProgress.kt            
│
└── MainActivity.kt                    
```

### Resource Structure
```
app/src/main/res/
├── 📂 layout/
│   ├── activity_main.xml              
│   ├── fragment_home.xml             
│   ├── fragment_search.xml         
│   ├── fragment_library.xml           
│   ├── fragment_player.xml            
│   ├── fragment_profile.xml          
│   ├── fragment_liked_songs.xml       
│   ├── fragment_playlists.xml        
│   ├── fragment_playlist_detail.xml   
│   ├── fragment_artists.xml           
│   ├── fragment_artist_detail.xml     
│   ├── fragment_downloads.xml         
│   ├── fragment_history.xml           
│   ├── fragment_login.xml              
│   ├── view_mini_player.xml          
│   ├── item_universal_track.xml      
│   ├── item_horizontal_track.xml       
│   ├── item_recent_track.xml         
│   ├── item_playlist.xml              
│   ├── item_artist_grid.xml           
│   ├── item_downloaded_track.xml      
│   ├── dialog_create_playlist.xml    
│   └── dialog_add_to_playlist.xml     
│
├── 📂 drawable/
│   ├── background_gradient.xml     
│   ├── bottom_nav_background.xml       
│   ├── ic_*.xml                       
│   └── badge_*.xml                   
│
├── 📂 navigation/
│   └── nav_graph.xml                
│
├── 📂 menu/
│   └── bottom_nav_menu.xml            
│
├── 📂 values/
│   ├── colors.xml                      
│   ├── strings.xml                    
│   └── themes.xml                    
│
└── 📂 xml/
    ├── backup_rules.xml                
    └── data_extraction_rules.xml      
```

## Screenshots

| Home | Search | Library |
|------|--------|---------|
| Recently played, Trending, Recommendations | Search tracks, albums, artists with filters | Playlists, Downloads, Liked Songs, Artists |

| Player | Mini Player | Profile |
|--------|-------------|---------|
| Full-screen player with controls | Persistent mini-player during navigation | User statistics and settings |

## Database Schema
```
┌─────────────────┐     ┌─────────────────────┐
│  liked_tracks   │     │      playlists      │
├─────────────────┤     ├─────────────────────┤
│ id (PK)         │     │ id (PK)             │
│ name            │     │ name                │
│ artistName      │     │ description         │
│ albumName       │     │ imageUrl            │
│ imageUrl        │     │ createdAt           │
│ previewUrl      │     │ updatedAt           │
│ durationMs      │     └──────────┬──────────┘
│ addedAt         │                │
└─────────────────┘                │
                                   │ 1:N
┌─────────────────┐     ┌──────────▼──────────┐
│followed_artists │     │   playlist_tracks   │
├─────────────────┤     ├─────────────────────┤
│ id (PK)         │     │ playlistId (FK)     │
│ name            │     │ trackId (PK)        │
│ imageUrl        │     │ trackName           │
│ followersCount  │     │ artistName          │
│ genres          │     │ imageUrl            │
│ addedAt         │     │ previewUrl          │
└─────────────────┘     │ addedAt             │
                        └─────────────────────┘

┌─────────────────┐     ┌─────────────────────┐
│ recently_played │     │  downloaded_tracks  │
├─────────────────┤     ├─────────────────────┤
│ trackId (PK)    │     │ trackId (PK)        │
│ trackName       │     │ trackName           │
│ artistName      │     │ artistName          │
│ albumName       │     │ localFilePath       │
│ imageUrl        │     │ fileSize            │
│ previewUrl      │     │ isFull              │
│ playedAt        │     │ downloadedAt        │
└─────────────────┘     └─────────────────────┘
```
