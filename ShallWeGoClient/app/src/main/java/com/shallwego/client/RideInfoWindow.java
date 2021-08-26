package com.shallwego.client;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.gson.JsonObject;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

public class RideInfoWindow extends MarkerInfoWindow {

    private final Activity callingActivity;
    private final JsonObject rideData;

    public RideInfoWindow(Activity callingActivity, MapView mapView, JsonObject data) {
        super(R.layout.destination_layout_main_activity, mapView);
        this.callingActivity = callingActivity;
        this.rideData = data;
    }

    @Override
    public Marker getMarkerReference() {
        return super.getMarkerReference();
    }

    @Override
    public void onOpen(Object item) {
        ImageView details = mView.findViewById(R.id.details);
        details.setOnClickListener((view) -> {
            Intent intent = new Intent(callingActivity, RideDetails.class);
            intent.putExtra("rideId", rideData.get("id").getAsInt());
            callingActivity.startActivity(intent);
        });
        TextView destination = mView.findViewById(R.id.text);
        destination.setText(rideData.get("lineIdentifier").getAsString() + " - " + rideData.get("destination").getAsString());
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
