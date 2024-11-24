package com.example.rentwise.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.rentwise.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ListTrackingFragment extends Fragment implements ListVehicleTrackFragment.OnVehicleItemClickListener {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker currentVehicleMarker;

    private final OnMapReadyCallback callback = map -> {
        googleMap = map;
        enableUserLocation();
        showUserLocation();

        // Handle marker click to open Google Maps for navigation
        googleMap.setOnMarkerClickListener(marker -> {
            LatLng position = marker.getPosition();
            openGoogleMapsForDirections(position);
            return false; 
        });
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        return inflater.inflate(R.layout.fragment_list_tracking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        ImageButton showBottomSheetButton = view.findViewById(R.id.showBottomSheetButton);
        showBottomSheetButton.setOnClickListener(v -> {
            ListVehicleTrackFragment bottomSheet = ListVehicleTrackFragment.newInstance("param1", "param2");
            bottomSheet.setOnVehicleItemClickListener(this);
            bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
        });

        ImageButton btnDirection = view.findViewById(R.id.btnDirection);
        btnDirection.setOnClickListener(v -> moveToUserLocation());
    }

    @Override
    public void onVehicleItemClick(double latitude, double longitude) {
        if (googleMap != null) {
            if (currentVehicleMarker != null) {
                currentVehicleMarker.remove();
            }

            String address = getAddressFromLatLng(latitude, longitude);

            LatLng vehicleLocation = new LatLng(latitude, longitude);
            BitmapDescriptor customIcon = getCustomMarkerIcon();

            currentVehicleMarker = googleMap.addMarker(new MarkerOptions()
                    .position(vehicleLocation)
                    .title(address != null ? address : "Không xác định") // Use address or fallback title
                    .icon(customIcon));

            currentVehicleMarker.showInfoWindow();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vehicleLocation, 15));
        } else {
            Toast.makeText(requireContext(), "Map is not ready!", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void showUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.addMarker(new MarkerOptions()
                        .position(userLocation)
                        .title("Vị trí của bạn"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            } else {
                Toast.makeText(requireContext(), "Unable to determine current location. Please check GPS!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void moveToUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            } else {
                Toast.makeText(requireContext(), "Unable to move to your location.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private BitmapDescriptor getCustomMarkerIcon() {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image_unsplash_jlf_jndeo1); // Replace with your custom icon
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, false); // Resize icon
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }

    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0); // Get the full address
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void openGoogleMapsForDirections(LatLng destination) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destination.latitude + "," + destination.longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(requireContext(), "Google Maps is not installed on this device.", Toast.LENGTH_SHORT).show();
        }
    }
}
