package com.shallwego.client;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;


public class PathFragment extends Fragment {

    private JsonArray path;
    private final ArrayList<GeoPoint> points = new ArrayList<>();

    public PathFragment() {}


    public PathFragment(JsonArray path) {
        this.path = path;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_path, container, false);
        LinearLayout layout = v.findViewById(R.id.container);
        int i = 0;
        int target = path.size();
        for (JsonElement stop: path) {
           JsonObject stopObject = (JsonObject) stop;
           if (i == 0) {
               View firstStop = getLayoutInflater().inflate(R.layout.line_details_first_stop_layout, null);
               MaterialTextView stopName = firstStop.findViewById(R.id.stopNameFirst);
               stopName.setText(stopObject.get("name").toString().replace("\"", ""));
               ImageView imageView = firstStop.findViewById(R.id.stopDetailsFirst);
               imageView.setOnClickListener((view) -> {
                   Intent intent = new Intent(getActivity(), StopDetails.class);
                   intent.putExtra("stopId", stopObject.get("stopId").toString().replace("\"", ""));
                   startActivity(intent);
               });
               layout.addView(firstStop, i++);
           } else if (i == target - 1) {
               View lastStop = getLayoutInflater().inflate(R.layout.line_details_last_stop_layout, null);
               MaterialTextView stopName = lastStop.findViewById(R.id.stopNameLast);
               stopName.setText(stopObject.get("name").toString().replace("\"", ""));
               ImageView imageView = lastStop.findViewById(R.id.stopDetailsLast);
               imageView.setOnClickListener((view) -> {
                   Intent intent = new Intent(getActivity(), StopDetails.class);
                   intent.putExtra("stopId", stopObject.get("stopId").toString().replace("\"", ""));
                   startActivity(intent);
               });
               layout.addView(lastStop, i++);
           } else {
               View normalStop = getLayoutInflater().inflate(R.layout.line_details_normal_stop_layout, null);
               MaterialTextView stopName = normalStop.findViewById(R.id.stopNameNormal);
               stopName.setText(stopObject.get("name").toString().replace("\"", ""));
               ImageView imageView = normalStop.findViewById(R.id.stopDetailsLast);
               imageView.setOnClickListener((view) -> {
                   Intent intent = new Intent(getActivity(), StopDetails.class);
                   intent.putExtra("stopId", stopObject.get("stopId").toString().replace("\"", ""));
                   startActivity(intent);
               });
               layout.addView(normalStop, i++);
           }
           points.add(new GeoPoint(Double.parseDouble(stopObject.get("latitude").toString().replace("\"", "")), Double.parseDouble(stopObject.get("longitude").toString().replace("\"", ""))));
        }
        MapView map = v.findViewById(R.id.lineDetailsMap);
        setUpMap(map);

        return v;
    }

    private void setUpMap(MapView map) {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setOnTouchListener((v, event) -> true);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setMultiTouchControls(true);
        map.getOverlays().add(new CopyrightOverlay(getActivity()));
        ArrayList<GeoPoint> boundingBoxPoints = new ArrayList<>();
        boundingBoxPoints.add(points.get(0));
        boundingBoxPoints.add(points.get(points.size() - 1));
        map.addOnFirstLayoutListener((v, left, top, right, bottom) -> {
            BoundingBox b = BoundingBox.fromGeoPoints(boundingBoxPoints);
            map.zoomToBoundingBox(b,false,100);
            map.invalidate();
        });

        new Thread(() -> {
            OSRMRoadManager osrmRoadManager = new OSRMRoadManager(getActivity(), Configuration.getInstance().getUserAgentValue());
            Road road = osrmRoadManager.getRoad(points);
            Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
            getActivity().runOnUiThread(() -> {
                map.getOverlays().add(roadOverlay);
                map.invalidate();
            });
        }).start();
        map.invalidate();
    }
}