package com.example.rentwise.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rentwise.ModelData.FirebaseRepository;
import com.example.rentwise.ModelData.Motobike;
import com.example.rentwise.ModelData.Quadruple;
import com.example.rentwise.MyAdapter;
import com.example.rentwise.R;
import com.example.rentwise.ModelData.Customer;
import com.example.rentwise.ModelData.GPS;
import com.example.rentwise.ModelData.Transaction;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import kotlin.Triple;

public class ListVehicleTrackFragment extends BottomSheetDialogFragment {



    // Thêm phương thức newInstance để tạo đối tượng với các tham số
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private RecyclerView recyclerView;
    private MyAdapter<Quadruple<Customer, GPS, Transaction, Motobike>> adapter;


    public static ListVehicleTrackFragment newInstance(String param1, String param2) {
        ListVehicleTrackFragment fragment = new ListVehicleTrackFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    public interface OnVehicleItemClickListener {
        void onVehicleItemClick(double latitude, double longitude);
    }

    private OnVehicleItemClickListener listener;


    public void setOnVehicleItemClickListener(OnVehicleItemClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_vehicle_track, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewVehicleTrack);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadData();
        return view;
    }

    private void loadData() {
        FirebaseRepository<Customer> customerRepository = new FirebaseRepository<>("Customer", Customer.class);
        FirebaseRepository<GPS> gpsRepository = new FirebaseRepository<>("GPS", GPS.class);
        FirebaseRepository<Transaction> transactionRepository = new FirebaseRepository<>("Transaction", Transaction.class);
        FirebaseRepository<Motobike> motobikeRepository = new FirebaseRepository<>("Motobike", Motobike.class);

        List<Customer> customerList = new ArrayList<>();
        List<GPS> gpsList = new ArrayList<>();
        List<Transaction> transactionList = new ArrayList<>();
        List<Motobike> motobikeList = new ArrayList<>();

        final int[] dataLoaded = {0};

        transactionRepository.getAll(new FirebaseRepository.OnFetchListListener<Transaction>() {
            @Override
            public void onSuccess(List<Transaction> data) {
                transactionList.addAll(data);
                dataLoaded[0]++;
                if (dataLoaded[0] == 4) {
                    combineData(customerList, gpsList, transactionList, motobikeList);
                }
            }

            @Override
            public void onFailure(String message) {
                Log.e("FirebaseRepository", message);
            }
        });

        gpsRepository.getAll(new FirebaseRepository.OnFetchListListener<GPS>() {
            @Override
            public void onSuccess(List<GPS> data) {
                gpsList.addAll(data);
                dataLoaded[0]++;
                if (dataLoaded[0] == 4) {
                    combineData(customerList, gpsList, transactionList, motobikeList);
                }
            }

            @Override
            public void onFailure(String message) {
                Log.e("FirebaseRepository", message);
            }
        });

        customerRepository.getAll(new FirebaseRepository.OnFetchListListener<Customer>() {
            @Override
            public void onSuccess(List<Customer> data) {
                customerList.addAll(data);
                dataLoaded[0]++;
                if (dataLoaded[0] == 4) {
                    combineData(customerList, gpsList, transactionList, motobikeList);
                }
            }

            @Override
            public void onFailure(String message) {
                Log.e("FirebaseRepository", message);
            }
        });

        motobikeRepository.getAll(new FirebaseRepository.OnFetchListListener<Motobike>() {
            @Override
            public void onSuccess(List<Motobike> data) {
                motobikeList.addAll(data);
                dataLoaded[0]++;
                if (dataLoaded[0] == 4) {
                    combineData(customerList, gpsList, transactionList, motobikeList);
                }
            }

            @Override
            public void onFailure(String message) {
                Log.e("FirebaseRepository", message);
            }
        });
    }

    private void combineData(List<Customer> customers, List<GPS> gpsList, List<Transaction> transactions, List<Motobike> motobikes) {
        List<Quadruple<Customer, GPS, Transaction, Motobike>> combinedList = new ArrayList<>();

        for (Transaction transaction : transactions) {
            GPS matchedGps = null;
            Customer matchedCustomer = null;
            Motobike matchedMotobike = null;

            for (GPS gps : gpsList) {
                if (gps.getIdVehicle().equals(transaction.getIdMotobike())) {
                    matchedGps = gps;
                    break;
                }
            }

            for (Customer customer : customers) {
                if (customer.getCccd().equals(transaction.getIdCustomer())) {
                    matchedCustomer = customer;
                    break;
                }
            }

            for (Motobike motobike : motobikes) {
                if (motobike.getNumberPlate().equals(transaction.getIdMotobike())) {
                    matchedMotobike = motobike;
                    break;
                }
            }

            if (matchedGps != null && matchedCustomer != null && matchedMotobike != null) {
                combinedList.add(new Quadruple<>(matchedCustomer, matchedGps, transaction, matchedMotobike));
            }
        }

        setupRecyclerView(combinedList);
    }

    private void setupRecyclerView(List<Quadruple<Customer, GPS, Transaction, Motobike>> combinedList) {
        Log.d("ListVehicleTrackFragment", "Setting up RecyclerView with " + combinedList.size() + " items");

        adapter = new MyAdapter<>(combinedList, R.layout.item_vehicle, new MyAdapter.Binder<Quadruple<Customer, GPS, Transaction, Motobike>>() {
            @Override
            public void bind(View itemView, Quadruple<Customer, GPS, Transaction, Motobike> data) {
                Customer customer = data.getFirst();
                GPS gps = data.getSecond();
                Transaction transaction = data.getThird();
                Motobike motobike = data.getFourth();

                TextView nameCustomer = itemView.findViewById(R.id.nameCustomer);
                TextView tvStatusOn = itemView.findViewById(R.id.tv_StatusOn);
                TextView locationCustomer = itemView.findViewById(R.id.locationCustomer);
                ImageView imgVehicle = itemView.findViewById(R.id.imgVehicle);
                ImageView imgStatusOn = itemView.findViewById(R.id.imgStatusOn);

                nameCustomer.setText(customer.getName());
                tvStatusOn.setText(motobike.getNumberPlate());
                locationCustomer.setText(transaction.getZoneRent());
                imgVehicle.setImageResource(R.drawable.nvx_gray_green);

                if ("Online".equalsIgnoreCase(motobike.getStatus())) {
                    imgStatusOn.setImageResource(R.drawable.img_status_online);
                } else {
                    imgStatusOn.setImageResource(R.drawable.img_status_offline);
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        try {

                            double latitude = Double.parseDouble(gps.getLatitude());
                            double longitude = Double.parseDouble(gps.getLongitude());
                            listener.onVehicleItemClick(latitude, longitude);
                        } catch (NumberFormatException e) {
                            Log.e("ListVehicleTrackFragment", "Invalid GPS coordinates: " + gps.getLatitude() + ", " + gps.getLongitude());
                        }
                    }
                });
            }
        });

        recyclerView.setAdapter(adapter);
    }
}