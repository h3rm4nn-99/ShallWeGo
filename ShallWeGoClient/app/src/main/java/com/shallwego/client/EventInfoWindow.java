package com.shallwego.client;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.gson.JsonObject;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

public class EventInfoWindow extends MarkerInfoWindow {

    private final Activity callingActivity;
    private final JsonObject eventData;

    public EventInfoWindow(Activity callingActivity, MapView mapView, JsonObject data) {
        super(R.layout.event_details_preview_main_activity, mapView);
        this.callingActivity = callingActivity;
        this.eventData = data;
    }

    @Override
    public Marker getMarkerReference() {
        return super.getMarkerReference();
    }

    @Override
    public void onOpen(Object item) {
        ImageView details = mView.findViewById(R.id.details);
        details.setOnClickListener((view) -> {
            Intent intent = new Intent(callingActivity, EventDetails.class);
            intent.putExtra("eventId", eventData.get("id").getAsString());
            callingActivity.startActivity(intent);
        });
        TextView eventType = mView.findViewById(R.id.text);
        eventType.setText(eventData.get("eventType").getAsString());
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
