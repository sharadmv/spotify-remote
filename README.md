<h1>Spotify Remote</h1>
<p>
<b>A Spotify App and an Android Application that lets you remotely control your Spotify desktop app.</b>
</p>

First of all, clone this repo.

<h2>Setup</h2>
<p>1. Android: Install SpotifyRemote.apk from the /dist folder</p>
<p>2. Spotify: This is a bit more complicated
First, become a Spotify developer: https://developer.spotify.com/technologies/apps/#developer-account

Then, take the /js folder. If you're in Windows, copy it to My Documents/Spotify (make it if necessary). If you're in Mac or Linux, copy it to ~/Spotify.
Then rename js to spotify-remote. Now restart Spotify, login and in the search bar, type in "spotify:app:spotify-remote".It should show up and a facebook auth dialog will pop up.
Once it's authed, you're ready to go
</p>

NOTE: Getting a user's playlists is not possible currently with the Spotify Apps API. Therefore, if you want to add your own playlists to the remote, you have to paste the URI of the playlist (right click on the playlist name and click "Copy Spotify URI") and paste it in the index.html (specifically, into the 'playlists' JS array) in spotify-remote. Right now, it has some of my playlists.
<h2>Using it</h2>
Once the spotify app is running, run the android app. It should also present a Facebook auth dialog. Once that is done, it should load your playlists and you are ready to go! Have fun listening!
