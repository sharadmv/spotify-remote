package com.sharad.android;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.flotype.bridge.Bridge;
import com.flotype.bridge.BridgeRemoteObject;

public class SpotifyRemoteActivity extends Activity {
	interface Spotify extends BridgeRemoteObject {
		void searchAndPlay(String song);

		void pause();

		void resume();

		void next();

		void previous();

		void volumeUp();

		void volumeDown();
	}

	Spotify spotify;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Bridge bridge = new Bridge().setApiKey("abcdefgh");
		setContentView(R.layout.main);
		try {
			bridge.connect();
			spotify = bridge.getService("spotify", Spotify.class);
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

			super.onCreate(savedInstanceState);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}