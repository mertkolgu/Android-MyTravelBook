package tr.com.mertkolgu.mytravelbook;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    protected static SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.matches("new")) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("tr.com.mertkolgu.mytravelbook", MODE_PRIVATE);
                    boolean firstTimeCheck = sharedPreferences.getBoolean("notFirstTime", false);

                    if (!firstTimeCheck) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16));
                        sharedPreferences.edit().putBoolean("notFirstTime", true).apply();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    //
                }

                @Override
                public void onProviderEnabled(String provider) {
                    //
                }

                @Override
                public void onProviderDisabled(String provider) {
                    //
                }
            };

            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    mMap.clear();

                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (lastLocation != null) {
                        LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 16));
                    }
                }
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastLocation != null) {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 16));
                }
            }
        } else {
            mMap.clear();
            int position = intent.getIntExtra("position", 0);
            LatLng location = new LatLng(MainActivity.locations.get(position).latitude, MainActivity.locations.get(position).longitude);
            String placeName = MainActivity.names.get(position);

            mMap.addMarker(new MarkerOptions().position(location).title(placeName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            if (requestCode == 1) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    Intent intent = getIntent();
                    String info = intent.getStringExtra("info");

                    if (info.matches("new")) {
                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if (lastLocation != null) {
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 16));
                        }
                    } else {
                        mMap.clear();
                        int position = intent.getIntExtra("position", 0);
                        LatLng location = new LatLng(MainActivity.locations.get(position).latitude, MainActivity.locations.get(position).longitude);
                        String placeName = MainActivity.names.get(position);

                        mMap.addMarker(new MarkerOptions().position(location).title(placeName));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String address = "";

        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addressList != null && addressList.size() > 0) {
                if (addressList.get(0).getThoroughfare() != null) {
                    address += addressList.get(0).getThoroughfare() + ", ";

                    if (addressList.get(0).getSubThoroughfare() != null) {
                        address += addressList.get(0).getSubThoroughfare() + ", ";

                        if (addressList.get(0).getSubAdminArea() != null) {
                            address += addressList.get(0).getSubAdminArea() + ", ";

                            if (addressList.get(0).getAdminArea() != null) {
                                address += addressList.get(0).getAdminArea() + ", ";

                                if (addressList.get(0).getCountryName() != null) {
                                    address += addressList.get(0).getCountryName();
                                }
                            }
                        }
                    }
                }
            } else {
                address = "New Place";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMap.addMarker(new MarkerOptions().position(latLng).title(address));
        Toast.makeText(this, "New Place Created!", Toast.LENGTH_LONG).show();

        MainActivity.names.add(address);
        MainActivity.locations.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();

        try {
            Double latitude = latLng.latitude;
            Double longitude = latLng.longitude;

            String coordinateLatitude = latitude.toString();
            String coordinateLongitude = longitude.toString();

            database = this.openOrCreateDatabase("Places", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS places (name VARCHAR, latitude VARCHAR, longitude VARCHAR)");
            String toCompile = "INSERT INTO places (name, latitude, longitude) VALUES (?, ?, ?)";

            SQLiteStatement sqLiteStatement = database.compileStatement(toCompile);
            sqLiteStatement.bindString(1, address);
            sqLiteStatement.bindString(2, coordinateLatitude);
            sqLiteStatement.bindString(3, coordinateLongitude);

            sqLiteStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
