# MusicPlayerApp (UnnamedPlayer)
Music player application with playback speed adjustment.

## Enviroments
- IDE: Android Studio
- minSdkVersion: 24
- targetSdkVersion: 28
### things used in this project
- SharedPrefereces (Custom Playlists)
- MediaPlayer (Basic musicplayer features)
- ContentProvider (Fetching musics in the storage)

## Features
- Load all music files from the storage.
- Basic music player features: play, pause/resume, prev, next, loop and playlist shuffling.
- Playback speed adjustmentment. (0.25x ~ 2.00x)
- Custom Playlists

## Details
- Musicplayer service (you can control it through foreground notification service).
- Playlists also have its playback speed.
- Detaching all earphones (bluetooth earphone included), phone calling can pause the player while playing.
- You can Add/Delete, Adjust playback speed, Rename each custom playlists.
- You can also delete actual music files. 

## Screenshots
### Main activity
<img src="https://user-images.githubusercontent.com/41889090/174980116-ec2f2313-58ca-4e5d-919b-25fcffbb9d68.jpg" width="300">

### Playlist
<img src="https://user-images.githubusercontent.com/41889090/174980317-abe64aaf-d9b4-4869-a66d-9073b2b4235c.jpg" width="300">
<img src="https://user-images.githubusercontent.com/41889090/174980442-8d612013-5ca1-43e8-96f0-365a6ef41d20.jpg" width="300">

### Service
     
<img src="https://i.imgur.com/GiS1Gwa.jpg" width="300">

### Play activity
<img src="https://user-images.githubusercontent.com/41889090/174980645-86000d28-a177-4ceb-830d-abdb74cef185.jpg" width="300">
<img src="https://user-images.githubusercontent.com/41889090/174980933-4f729eb0-1a7e-42aa-a194-f67b830806a8.jpg" height="350">

