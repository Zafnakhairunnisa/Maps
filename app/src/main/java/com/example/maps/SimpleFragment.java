package com.example.maps;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

public class SimpleFragment extends Fragment {

    private static final String ARGUMENT_MARKER_ID = "argument_marker_id";

    private GoogleMap mMap;
    private boolean oke = false;
    TextView lat, lon, address, name;

    public SimpleFragment() {
        // Required empty public constructor
    }



    public static SimpleFragment newInstance() {

        return new SimpleFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_simple, container, false);
        // Inflate the layout for this fragment
//       return inflater.inflate(R.layout.fragment_simple, container, false);

        lat = view.findViewById(R.id.latitudeMap);
        lon = view.findViewById(R.id.longitudeMap);
        address = view.findViewById(R.id.addressMap);
        name = view.findViewById(R.id.nameMap);

        

        return view;

    }
}