package com.sharad.android;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class VerifyActivity extends Activity {
	Facebook facebook = new Facebook("316216588456628");
	private SharedPreferences mPrefs;
	private AsyncFacebookRunner mAsyncRunner;
	private Handler handler;
	private String id;

	List<Playlist> playlists;
	boolean playing = false;

	private boolean touching = false;
	private int total = 0;

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

	Spinner spinner;

	PlaylistAdapter adapter;

	class PositionCallback implements BridgeObject {

		public void callback(final int pos) {
			handler.post(new Runnable() {

				public void run() {
					setPosition(pos);

				}

			});
		}
	}

	class NowPlayingCallback implements BridgeObject {
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
		handler = new Handler();
		final Bridge bridge = new Bridge().setApiKey("abcdefgh");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spotify);
		mPrefs = getPreferences(MODE_PRIVATE);
		mAsyncRunner = new AsyncFacebookRunner(facebook);
		facebook.authorize(this, new Facebook.DialogListener() {
			public void onComplete(Bundle values) {
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putString("access_token", facebook.getAccessToken());
				editor.putLong("access_expires", facebook.getAccessExpires());
				editor.commit();
				mAsyncRunner.request("me", new RequestListener() {

					public void onComplete(String response, Object state) {
						JSONObject obj;
						try {
							obj = new JSONObject(response);
							id = (String) obj.get("id");
							bridge.connect();
							spotify = bridge.getService(
									"spotify" + obj.get("id"), Spotify.class);
							spotify.setUpdateCallback(new NowPlayingCallback());
						} catch (JSONException e1) {
							e1.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						handler.post(new Runnable() {
							public void run() {

								spinner = (Spinner) findViewById(R.id.playlists);
								playlists = new ArrayList<Playlist>();
								adapter = new PlaylistAdapter(
										getApplicationContext(),
										R.layout.playlist, playlists);
								adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

								spinner.setAdapter(adapter);
								spinner.setSelection(0);
								((Button) findViewById(R.id.playplaylist))
										.setOnClickListener(new OnClickListener() {

											public void onClick(View arg0) {
												spotify.playlist(((Playlist) spinner
														.getSelectedItem())
														.getUri());
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
								if (spotify == null) {
									spotify = bridge.getService("spotify" + id,
											Spotify.class);
								}
								spotify.getPlaylists(new PlaylistCallback());
								spotify.getPosition(new PositionCallback());
								((SeekBar) findViewById(R.id.seekBar))
										.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

											public void onProgressChanged(
													SeekBar arg0, int progress,
													boolean arg2) {
												int remainder = (int) progress / 1000 % 60;
												int leftRemainder = (int) (total - progress) / 1000 % 60;
												((TextView) findViewById(R.id.time)).setText(progress
														/ 60000
														+ ":"
														+ ((remainder + "")
																.length() == 1 ? "0"
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

											public void onStopTrackingTouch(
													SeekBar arg0) {
												touching = false;
												spotify.setPosition(arg0
														.getProgress());
												setPosition(arg0.getProgress());

											}

										});
								Runnable r = new Runnable() {

									public void run() {
										while (true) {
											handler.post(new Runnable() {
												public void run() {
													spotify.getPosition(new PositionCallback());
												}
											});
											try {
												Thread.sleep(250);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
									}

								};
								new Thread(r).start();
							}
						});
					}

					public void onIOException(IOException e, Object state) {

					}

					public void onFileNotFoundException(
							FileNotFoundException e, Object state) {

					}

					public void onMalformedURLException(
							MalformedURLException e, Object state) {

					}

					public void onFacebookError(FacebookError e, Object state) {
						// TODO Auto-generated method stub

					}
				});
			}

			public void onFacebookError(FacebookError error) {
				System.out.println(error.getMessage());
			}

			public void onError(DialogError e) {
				System.out.println(e.getMessage());
			}

			public void onCancel() {
				System.out.println("cancel");
			}
		});
		// ((Button) findViewById(R.id.connect))
		// .setOnClickListener(new OnClickListener() {
		// public void onClick(View view) {
		// Intent intent = new Intent(view.getContext(),
		// SpotifyRemoteActivity.class);
		// intent.putExtra("id",
		// ((TextView) findViewById(R.id.id)).getText()
		// .toString());
		// startActivity(intent);
		// }
		// });
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	public void onResume() {
		super.onResume();
		facebook.extendAccessTokenIfNeeded(this, null);
	}

	@SuppressWarnings("unchecked")
	public void setNowPlaying(final LinkedHashMap<String, Object> song,
			final boolean play) {
		if (!((LinkedHashMap<String, Object>) song.get("album")).get("cover")
				.equals("")) {
			downloadBitmap(
					"https://d3rt1990lpmkn.cloudfront.net/640/"
							+ ((String) ((LinkedHashMap<String, Object>) song
									.get("album")).get("cover"))
									.split("spotify:image:")[1],
					((ImageView) findViewById(R.id.album)));
		}
		((TextView) findViewById(R.id.now)).setText("Now Playing: "
				+ song.get("name"));
		total = (Integer) song.get("duration");
		((SeekBar) findViewById(R.id.seekBar)).setMax(total);
		playing = play;

	}

	public void setPosition(int pos) {
		if (!touching)
			((SeekBar) findViewById(R.id.seekBar)).setProgress(pos);
	}

	static Bitmap downloadBitmap(String url) {
		final AndroidHttpClient client = AndroidHttpClient
				.newInstance("Android");
		final HttpGet getRequest = new HttpGet(url);

		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.w("ImageDownloader", "Error " + statusCode
						+ " while retrieving bitmap from " + url);
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();
					final Bitmap bitmap = BitmapFactory
							.decodeStream(inputStream);
					return bitmap;
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (Exception e) {
			// Could provide a more explicit error message for IOException or
			// IllegalStateException
			e.printStackTrace();
			getRequest.abort();
			Log.w("ImageDownloader", "Error while retrieving bitmap from "
					+ url);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return null;
	}

	public void downloadBitmap(String url, ImageView imageView) {
		BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
		task.execute(url);
	}

	class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;

		public BitmapDownloaderTask(ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		// Actual download method, run in the task thread
		protected Bitmap doInBackground(String... params) {
			// params comes from the execute() call: params[0] is the url.
			return downloadBitmap(params[0]);
		}

		@Override
		// Once the image is downloaded, associates it to the imageView
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}

			if (imageViewReference != null) {
				ImageView imageView = imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}

	}
	/* class BitmapDownloaderTask, see below */
}
