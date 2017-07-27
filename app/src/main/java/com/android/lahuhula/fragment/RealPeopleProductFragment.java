package com.android.lahuhula.fragment;

//import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.lahuhula.R;

/**
 * Created by lenovo on 2016/12/27.
 */

public class RealPeopleProductFragment extends Fragment {
    public RealPeopleProductFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.real_people_product_fragment_layout, container, false);
        return rootView;
    }
}
