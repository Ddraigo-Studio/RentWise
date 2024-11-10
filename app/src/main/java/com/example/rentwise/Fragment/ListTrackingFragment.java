package com.example.rentwise.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

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
import com.google.android.gms.maps.model.MarkerOptions;

public class ListTrackingFragment extends Fragment implements ListVehicleTrackFragment.OnVehicleItemClickListener {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private OnMapReadyCallback callback = map -> {
        googleMap = map;
        enableUserLocation();
        showUserLocation();
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
                //moveToUserLocation();
                showUserLocation();
            }
        }
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
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
                googleMap.addMarker(new MarkerOptions().position(userLocation).title("Vị trí của bạn"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
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
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
            }
        });
    }

    @Override
    public void onVehicleItemClick(double latitude, double longitude) {
        if (googleMap != null) {
            LatLng vehicleLocation = new LatLng(latitude, longitude);
            googleMap.clear();

            // Load the original bitmap from the drawable resource
            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image_unsplash_jlf_jndeo3);

            // Resize the bitmap (scale it down to, for example, 50x50 pixels)
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 80, 80, false);

            // Create a BitmapDescriptor from the resized bitmap
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);

            // Set the resized icon for the vehicle location marker
            googleMap.addMarker(new MarkerOptions()
                    .position(vehicleLocation)
                    .title("Vị trí của xe")
                    .icon(icon));

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vehicleLocation, 10));
        }
    }

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
}
