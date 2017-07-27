package com.android.lahuhula.fragment;

//import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.lahuhula.activity.FriendsCircleActivity;

import com.android.lahuhula.R;

/**
 * Created by lenovo on 2016/12/27.
 */

public class FriendsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = FriendsFragment.class.getSimpleName();

    public FriendsFragment(){}

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.friends_layout, container, false);
        LinearLayout friendsCircle = (LinearLayout) rootView.findViewById(R.id.friends_circle_layout_id);
        friendsCircle.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.friends_circle_layout_id:
                getContext().startActivity(new Intent(getContext(), FriendsCircleActivity.class));
                break;
        }
    }
}
