package com.h3rm4nn.locationtest;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

public class MyMarkerInfoWindow extends MarkerInfoWindow {
    private GeoPoint moreData;


    /**
     * @param layoutResId layout that must contain these ids: bubble_title,bubble_description,
     *                    bubble_subdescription, bubble_image
     * @param mapView
     */
    public MyMarkerInfoWindow(int layoutResId, MapView mapView, GeoPoint point) {
        super(layoutResId, mapView);
        this.moreData = point;
    }

    @Override
    public void onOpen(Object item) {
        Button b = mView.findViewById(R.id.bttn);
        b.setText("Coordinate?");
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mView.getContext(), "Latitudine " + moreData.getLatitude() + "\nLongitudine " + moreData.getLongitude(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClose() {}
}
