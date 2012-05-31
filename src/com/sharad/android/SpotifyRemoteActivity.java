package com.sharad.android;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.flotype.bridge.Bridge;
import com.flotype.bridge.BridgeObject;
import com.flotype.bridge.BridgeRemoteObject;
import com.sharad.android.model.Playlist;

public class SpotifyRemoteActivity extends Activity {
	List<Playlist> playlists;
	boolean playing = false;

	private boolean sleeping = false;
	private boolean touching = false;
	private int total = 0;
	private int current = 0;

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

		void setUpdateCallback(NowPlayingCallback callback);

		void getPosition(PositionCallback callback);

		void setPosition(int pos);
	}

	Handler handler = new Handler();

	Spinner spinner;

	PlaylistAdapter adapter;

	class PositionCallback implements BridgeObject {
		Handler handler;

		public PositionCallback(Handler handler) {
			this.handler = handler;
		}

		public void callback(final int pos) {
			current = pos;
			handler.post(new Runnable() {

				public void run() {
					setPosition(pos);

				}

			});
		}
	}

	class NowPlayingCallback implements BridgeObject {
		Handler handler;

		public NowPlayingCallback(Handler handler) {
			this.handler = handler;
		}

		public void callback(final LinkedHashMap<String, Object> song,
				final boolean playing) {
			handler.post(new Runnable() {
				public void run() {
					setNowPlaying(song, playing);
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
	Facebook facebook = new Facebook("316216588456628");
	private SharedPreferences mPrefs;
	private AsyncFacebookRunner mAsyncRunner;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler();
		final Bridge bridge = new Bridge().setApiKey("abcdefgh");
		mPrefs = getPreferences(MODE_PRIVATE);
		mAsyncRunner = new AsyncFacebookRunner(facebook);
		setContentView(R.layout.spotify);
		java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
		java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
		facebook.authorize(this, new Facebook.DialogListener() {
			public void onComplete(Bundle values) {
				System.out.println("SUP");
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putString("access_token", facebook.getAccessToken());
				editor.putLong("access_expires", facebook.getAccessExpires());
				editor.commit();
				mAsyncRunner.request("me", new RequestListener() {

					public void onComplete(String response, Object state) {
						JSONObject obj;
						try {
							obj = new JSONObject(response);
							System.out.println(obj.get("id"));
							bridge.connect();
							spotify = bridge.getService(
									"spotify" + obj.get("id"), Spotify.class);
							spotify.setUpdateCallback(new NowPlayingCallback(
									handler));
						} catch (JSONException e1) {
							e1.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						spinner = (Spinner) findViewById(R.id.playlists);
						playlists = new ArrayList<Playlist>();
						adapter = new PlaylistAdapter(getApplicationContext(),
								R.layout.playlist, playlists);
						adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

						spinner.setAdapter(adapter);
						spinner.setSelection(0);
						((Button) findViewById(R.id.playplaylist))
								.setOnClickListener(new OnClickListener() {

									public void onClick(View arg0) {
										spotify.playlist(((Playlist) spinner
												.getSelectedItem()).getUri());
									}

								});
						((ImageButton) findViewById(R.id.playButton))
								.setOnClickListener(new OnClickListener() {

									public void onClick(View arg0) {
										if (playing) {
											spotify.pause();
										} else {
											spotify.resume();
										}
									}

								});

						((ImageButton) findViewById(R.id.nextButton))
								.setOnClickListener(new OnClickListener() {

									public void onClick(View arg0) {
										spotify.next();
									}

								});

						((ImageButton) findViewById(R.id.previousButton))
								.setOnClickListener(new OnClickListener() {

									public void onClick(View arg0) {
										spotify.previous();
									}

								});
						spotify.getPlaylists(new PlaylistCallback(handler));
						spotify.getPosition(new PositionCallback(handler));
						((SeekBar) findViewById(R.id.seekBar))
								.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

									public void onProgressChanged(SeekBar arg0,
											int progress, boolean arg2) {
										int remainder = (int) progress / 1000 % 60;
										int leftRemainder = (int) (total - progress) / 1000 % 60;
										((TextView) findViewById(R.id.time)).setText(progress
												/ 60000
												+ ":"
												+ ((remainder + "").length() == 1 ? "0"
														+ remainder
														: remainder));
										((TextView) findViewById(R.id.left)).setText((total - progress)
												/ 60000
												+ ":"
												+ ((leftRemainder + "")
														.length() == 1 ? "0"
														+ leftRemainder
														: leftRemainder));
									}

									public void onStartTrackingTouch(
											SeekBar arg0) {
										touching = true;
									}

									public void onStopTrackingTouch(SeekBar arg0) {
										touching = false;
										spotify.setPosition(arg0.getProgress());
										setPosition(arg0.getProgress());

									}

								});
						Runnable r = new Runnable() {

							public void run() {
								while (true) {
									sleeping = false;
									handler.post(new Runnable() {
										public void run() {
											spotify.getPosition(new PositionCallback(
													handler));
										}
									});
									try {
										sleeping = true;
										Thread.sleep(250);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}

						};
						new Thread(r).start();
					}

					public void onIOException(IOException e, Object state) {
						e.printStackTrace();
					}

					public void onFileNotFoundException(
							FileNotFoundException e, Object state) {
						e.printStackTrace();
					}

					public void onMalformedURLException(
							MalformedURLException e, Object state) {
						e.printStackTrace();
					}

					public void onFacebookError(FacebookError e, Object state) {
						e.printStackTrace();

					}
				});
			}

			public void onFacebookError(FacebookError error) {
				System.out.println("ERROR: " + error.getMessage());
			}

			public void onError(DialogError error) {
				System.out.println("ERROR: " + error.getMessage());
			}

			public void onCancel() {
				System.out.println("cancel");
			}
		});
	}

	public void setNowPlaying(LinkedHashMap<String, Object> song,
			boolean playing) {
		((TextView) findViewById(R.id.now)).setText("Now Playing: "
				+ song.get("name"));
		total = (Integer) song.get("duration");
		((SeekBar) findViewById(R.id.seekBar)).setMax(total);
		this.playing = playing;
	}

	public void setPosition(int pos) {
		if (!touching)
			((SeekBar) findViewById(R.id.seekBar)).setProgress(pos);
	}
}