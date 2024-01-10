package com.example.boxapp3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import dagger.hilt.android.AndroidEntryPoint;

public class MenuTargetFragment extends Fragment {

    int layoutId1; // Replace with your variable

    public MenuTargetFragment(int layoutId) {
        this.layoutId1 = layoutId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(layoutId1, container, false);
    }
}