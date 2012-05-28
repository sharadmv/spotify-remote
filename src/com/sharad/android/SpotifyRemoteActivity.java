package com.sharad.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.flotype.bridge.Bridge;
import com.flotype.bridge.BridgeObject;
import com.flotype.bridge.BridgeRemoteObject;
import com.sharad.android.model.Playlist;

public class SpotifyRemoteActivity extends Activity {
	List<Playlist> playlists;

	interface Spotify extends BridgeRemoteObject {
		void searchAndPlay(String song);

		void pause();

		void resume();

		void next();

		void previous();

		void volumeUp();

		void volumeDown();

		void playlist(String string);

		List<Playlist> getPlaylists(PlaylistCallback callback);

		void setNowCallback(NowPlayingCallback callback);
	}

	Handler handler = new Handler();

	Spinner spinner;

	PlaylistAdapter adapter;

	class NowPlayingCallback implements BridgeObject {
		Handler handler;

		public NowPlayingCallback(Handler handler) {
			this.handler = handler;
		}

		public void callback(final String song) {
			handler.post(new Runnable() {

				public void run() {
					setNowPlaying(song);
				}

			});
		}
	}

	class PlaylistCallback implements BridgeObject {
		Handler handler;

		public PlaylistCallback(Handler handler) {
			this.handler = handler;

		}

		public void callback(final List<LinkedHashMap<String, Object>> plays) {
			handler.post(new Runnable() {

				public void run() {
					for (LinkedHashMap<String, Object> l : plays)
						adapter.add(new Playlist((String) l.get("name"),
								(String) l.get("uri"), (Integer) l
										.get("length")));
				}

			});
		}
	}

	Spotify spotify;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Bridge bridge = new Bridge().setApiKey("abcdefgh");
		setContentView(R.layout.spotify);
		try {
			bridge.connect();
			spotify = bridge
					.getService("spotify" + getIntent().getStringExtra("id"),
							Spotify.class);
			spotify.setNowCallback(new NowPlayingCallback(handler));
			spinner = (Spinner) findViewById(R.id.playlists);
			playlists = new ArrayList<Playlist>();
			adapter = new PlaylistAdapter(getApplicationContext(),
					R.layout.playlist, playlists);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			spinner.setAdapter(adapter);
			spinner.setSelection(0);
			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				public void onItemSelected(AdapterView<?> parent, View view,
						int pos, long id) {
					System.out.println(playlists.get(pos));
				}

				public void onNothingSelected(AdapterView<?> arg0) {

				}

			});
			((Button) findViewById(R.id.playplaylist))
					.setOnClickListener(new OnClickListener() {

						public void onClick(View arg0) {
							spotify.playlist(((Playlist) spinner
									.getSelectedItem()).getUri());
						}

					});
			((Button) findViewById(R.id.play))
					.setOnClickListener(new OnClickListener() {

						public void onClick(View arg0) {
							spotify.searchAndPlay(((EditText) findViewById(R.id.song))
									.getText().toString());
						}

					});
			((Button) findViewById(R.id.pause))
					.setOnClickListener(new OnClickListener() {

						public void onClick(View arg0) {
							spotify.pause();
						}

					});
			((Button) findViewById(R.id.resume))
					.setOnClickListener(new OnClickListener() {

						public void onClick(View arg0) {
							spotify.resume();
						}

					});
			((Button) findViewById(R.id.next))
					.setOnClickListener(new OnClickListener() {

						public void onClick(View arg0) {
							spotify.next();
						}

					});

			((Button) findViewById(R.id.previous))
					.setOnClickListener(new OnClickListener() {

						public void onClick(View arg0) {
							spotify.previous();
						}

					});
			((Button) findViewById(R.id.vUp))
					.setOnClickListener(new OnClickListener() {

						public void onClick(View arg0) {
							spotify.volumeUp();
						}

					});

			((Button) findViewById(R.id.vDown))
					.setOnClickListener(new OnClickListener() {

						public void onClick(View arg0) {
							spotify.volumeDown();
						}

					});
			spotify.getPlaylists(new PlaylistCallback(handler));

			super.onCreate(savedInstanceState);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setNowPlaying(String song) {
		((TextView) findViewById(R.id.now)).setText("Now Playing: " + song);
	}

}