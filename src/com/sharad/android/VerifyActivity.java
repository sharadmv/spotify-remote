package com.sharad.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class VerifyActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout);
		((Button) findViewById(R.id.connect))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						Intent intent = new Intent(view.getContext(),
								SpotifyRemoteActivity.class);
						intent.putExtra("id",
								((TextView) findViewById(R.id.id)).getText()
										.toString());
						startActivity(intent);
					}
				});
	}

}