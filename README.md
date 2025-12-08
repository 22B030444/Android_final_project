# Hearo Android App

## Team Members
| Student Name          | Student ID   |
|-----------------------|--------------|
| Suleimenova Zhasmin   | 22B030444    |
| Bexeit Alua           | 22B030283    |
## Table of Contents
- [Project Overview](#project-overview)
- [Key Features](#key-features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Project Structure](#project-structure)

## Project Overview
**Hearo** is a Kotlin-based Android app built using Android Studio that offers users a clean interface to access and enjoy audio content. The app supports features like login authentication, media player controls, and smooth UI transitions, all while adhering to modern Android development principles like MVVM (Model-View-ViewModel) architecture.

## Key Features
- **Smooth User Interface**: Fully responsive UI with a clean, intuitive design.
- **User Authentication**: Uses a simple login screen for user management.
- **Playlist Management**: Create, edit, and organize your music into playlists.
- **Media Playback**: Play, pause, skip, shuffle, and volume control functionality.

## Requirements
- **Android Studio**: Latest stable version of Android Studio.
- **JDK**: Version 11 or higher.
- **Android SDK**: Ensure you have the necessary Android SDK components.
- **Kotlin**: The app is built using Kotlin (ensure your Android Studio setup supports Kotlin).

## Installation

### Clone the Repository
Clone the repository to your local machine:

```bash
git clone https://github.com/22B030444/Android_final_project/
cd hearo
```

## Project structure
```bash
app/
├── 📂 data/
│   ├── 📂 api/
│   │   ├── MusicApiService.kt          
│   │   └── RetrofitClient.kt
│   ├── 📂 database/
│   │   ├── MusicDatabase.kt           
│   │   ├── dao/
│   │   │   ├── TrackDao.kt
│   │   │   ├── PlaylistDao.kt
│   │   │   └── ArtistDao.kt
│   │   └── entity/
│   │       ├── TrackEntity.kt
│   │       ├── PlaylistEntity.kt
│   │       └── ArtistEntity.kt
│   ├── 📂 preferences/
│   │   └── AppPreferences.kt           
│   ├── 📂 model/
│   │   ├── SearchResponse.kt
│   │   ├── Track.kt
│   │   └── UiState.kt
│   └── 📂 repository/
│       └── MusicRepository.kt         
│
├── 📂 ui/
│   ├── 📂 home/
│   │   ├── HomeFragment.kt
│   │   └── HomeViewModel.kt            
│   ├── 📂 search/
│   │   ├── SearchFragment.kt
│   │   ├── SearchViewModel.kt
│   │   └── GenreFragment.kt
│   ├── 📂 library/
│   │   ├── LibraryFragment.kt
│   │   ├── LikedSongsFragment.kt
│   │   ├── PlaylistsFragment.kt
│   │   ├── ArtistsFragment.kt
│   │   ├── DownloadsFragment.kt
│   │   └── LibraryViewModel.kt
│   ├── 📂 player/
│   │   ├── PlayerFragment.kt
│   │   ├── PlayerViewModel.kt
│   │   └── MediaPlayerManager.kt
│   ├── 📂 artist/
│   │   ├── ArtistDetailFragment.kt
│   │   └── ArtistViewModel.kt
│   ├── 📂 adapter/
│   │   ├── TrackAdapter.kt
│   │   ├── GenreAdapter.kt
│   │   ├── PlaylistAdapter.kt
│   │   └── ArtistAdapter.kt
│   └── MainActivity.kt
│
└── 📂 utils/
    ├── Extensions.kt
    ├── Constants.kt
    └── ViewModelFactory.kt

```
  
