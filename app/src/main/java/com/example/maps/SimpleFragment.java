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
    TextView mLat, mLon, mAddress, mName;

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

        mLat = view.findViewById(R.id.latitudeMap);
        mLon = view.findViewById(R.id.longitudeMap);
        mAddress = view.findViewById(R.id.addressMap);
        mName = view.findViewById(R.id.nameMap);

        Bundle args = getArguments();
        if (args != null) {
            String name = args.getString("name");
            String address = args.getString("address");
            double latitude = args.getDouble("latitude");
            double longitude = args.getDouble("longitude");

            mName.setText(name);
            mAddress.setText(address);
            mLat.setText(String.valueOf(latitude) + ", ");
            mLon.setText(String.valueOf(longitude));

        }


        return view;

    }
}