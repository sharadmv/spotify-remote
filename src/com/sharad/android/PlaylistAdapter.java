package com.sharad.android;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sharad.android.model.Playlist;

public class PlaylistAdapter extends ArrayAdapter<Playlist> {
	private List<Playlist> items;

	public PlaylistAdapter(Context context, int textViewResourceId,
			List<Playlist> objects) {
		super(context, textViewResourceId, objects);
		this.items = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.playlist, parent, false);
		}
		Playlist o = items.get(position);
		if (o != null) {
			TextView tt = (TextView) v.findViewById(R.id.text);
			if (tt != null) {
				tt.setText(o.getName());
			}
		}
		return v;
	}

}
