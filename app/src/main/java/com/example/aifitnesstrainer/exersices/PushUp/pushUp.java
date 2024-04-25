package com.example.aifitnesstrainer.exersices.PushUp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import com.example.aifitnesstrainer.R;
import com.example.aifitnesstrainer.exersices.Lateral_raise.lateral_raises_goal;

public class pushUp extends Fragment {

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    public pushUp() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_push_up, container, false);
        WebView gifWebView = view.findViewById(R.id.gifWebView);
        gifWebView.loadUrl("file:///android_res/drawable/pushup.gif");
        Button pushUp=view.findViewById(R.id.pushUp_camera);
        pushUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new pushUp_goal());
            }
        });
        return view;
    }
}