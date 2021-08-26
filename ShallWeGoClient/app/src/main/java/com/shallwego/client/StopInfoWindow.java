package com.shallwego.client;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

public class StopInfoWindow extends MarkerInfoWindow {

    private final Activity activity;
    private final JsonObject stopData;

    public StopInfoWindow(Activity callingActivity, MapView mapView, JsonObject data) {
        super(R.layout.stop_preview_layout_main_activity, mapView);
        this.activity = callingActivity;
        this.stopData = data;
    }

    @Override
    public Marker getMarkerReference() {
        return super.getMarkerReference();
    }

    @Override
    public void onOpen(Object item) {
        ImageView details = mView.findViewById(R.id.details);
        details.setOnClickListener((view) -> {
            Intent intent = new Intent(activity, StopDetails.class);
            intent.putExtra("stopId", stopData.get("id").getAsString());
            activity.startActivity(intent);
        });
        TextView stopName = mView.findViewById(R.id.name);
        stopName.setText(stopData.get("name").getAsString());
        ChipGroup lines = mView.findViewById(R.id.chipGroup);
        lines.removeAllViews();
        JsonArray linesArray = stopData.get("lines").getAsJsonArray();
        linesArray.forEach((line) -> {
            Chip currentLine = new Chip(mView.getContext());
            currentLine.setText(line.getAsString());
            lines.addView(currentLine);
        });
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
