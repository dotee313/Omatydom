package com.dotee313.omatydom.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dotee313.omatydom.core.GameEvent;

/** Turns chronicle entries into two-line list rows. */
public class EventAdapter extends ArrayAdapter<GameEvent> {

    public EventAdapter(Context context) {
        super(context, R.layout.event_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.event_item, parent, false);
        }

        TextView title = view.findViewById(R.id.eventTitle);
        TextView detail = view.findViewById(R.id.eventDetail);
        GameEvent event = getItem(position);

        title.setText(event.getDay() + " · " + event.getType().getDisplayName()
                + " · " + event.getTitle());
        detail.setText(event.getDescription());
        return view;
    }
}
