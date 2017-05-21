package com.agnik.priyankakargupta.speediq;

import android.*;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class SpeedActivity extends AppCompatActivity implements LocationListener{

    private TextView speedval;
    private TextView locationval;
    private double lt;
    private double lg;
    private LocationManager lm;
    private Geocoder geocoder;
    private List<Address> addresses;
    private String address;
    private Button goback;
    private Button viewreports;
    GoogleMap map;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed);
        speedval = (TextView) findViewById(R.id.speedval);
        locationval = (TextView) findViewById(R.id.locationval);
        viewreports = (Button) findViewById(R.id.viewreports);
        viewreports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SpeedActivity.this, EventsActivity.class);
                startActivity(intent);
            }
        });
        goback = (Button) findViewById(R.id.goback);
        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SpeedActivity.this, CameraView.class);
                startActivity(intent);
            }
        });
        type = CameraView.classification;
        DecimalFormat df = new DecimalFormat("#.##");
        speedval.setText(df.format(CameraView.speed) + " MPH");

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
                    1);
        }
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send user to the GPS settings
        if (!enabled) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Your location is not enabled. Would you like to enable it so that you can use this app?");
            alertDialogBuilder.setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    Intent myIntent = new Intent(SpeedActivity.this, SpeedActivity.class);
                    SpeedActivity.this.startActivity(myIntent);
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                         int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);
        lm.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                0, this);
        Location current = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(current.getLatitude(), current.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        address = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getLocality()
                + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName()
                + ", " + addresses.get(0).getPostalCode();
        map = ((MapFragment) getFragmentManager().
                findFragmentById(R.id.map)).getMap();
        // Add Map of speeding event
        Marker m = map.addMarker(new MarkerOptions()
                .position(new LatLng(current.getLatitude(), current.getLongitude()))
                .title(speedval.getText()+ ": " + type)
        );

        // Move camera to the marker
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(current.getLatitude(), current.getLongitude()), 15));

    }

    @Override
    public void onLocationChanged(Location location) {
        lt = location.getLatitude();
        lg = location.getLongitude();
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lt, lg, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        address = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getLocality()
                + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName()
        + ", " + addresses.get(0).getPostalCode();

        locationval.setText(address);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
