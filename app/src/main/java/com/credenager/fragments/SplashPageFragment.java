package com.credenager.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.credenager.utils.Globals;
import com.credenager.R;
import com.credenager.utils.Api;

public class SplashPageFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.splash_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        float width = Resources.getSystem().getDisplayMetrics().widthPixels - Globals.dpToPx(40, requireContext());
        view.findViewById(R.id.header_image).getLayoutParams().width = width > 900 ? 900 : (int) width;

        String token = Globals.getToken(requireContext());

        if (token == null) {
            new Handler().postDelayed(() -> gotoWelcomePage(view), 300);
        }
        else {
            TextView status = view.findViewById(R.id.welcome_page_status);
            status.setText("Verifying User...");

            Api.getUserBasic(token, response -> {
                try {
                    int responseCode = response.getInt("code");
                    if (responseCode == 200) {
                        final String email = response.getString("email");
                        Globals.APP_OFFLINE_MODE = false;
                        Globals.setUserState(email, token);
                        new Handler(Looper.getMainLooper()).post(() -> gotoKeyPage(view));
                    }
                    else if (responseCode == 502) {
                        boolean offlineMode = Globals.getOfflineSetting(requireContext());
                        String storedKey = Globals.getKey(requireContext());
                        String storedData = Globals.getData(requireContext());

                        if (offlineMode && storedKey != null && storedData != null) {
                            Globals.APP_OFFLINE_MODE = true;
                            Globals.setUserState(null, token);
                            new Handler(Looper.getMainLooper()).post(() -> gotoKeyPage(view));
                        }
                        else
                            new Handler(Looper.getMainLooper()).post(() -> gotoWelcomePage(view));
                    }
                    else
                        new Handler(Looper.getMainLooper()).post(() -> gotoWelcomePage(view));
                }
                catch (Exception e){
                    new Handler(Looper.getMainLooper()).post(() -> gotoWelcomePage(view));
                }
            });
        }
    }

    private void gotoKeyPage(View view) {
        int[] coordinates = new int[2];
        view.findViewById(R.id.splash_top).getLocationOnScreen(coordinates);
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new KeyPageFragment(coordinates[0], coordinates[1])).commit();
    }

    private void gotoWelcomePage(View view) {
        int[] coordinates = new int[2];
        view.findViewById(R.id.splash_top).getLocationOnScreen(coordinates);
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new WelcomePageFragment(coordinates[0], coordinates[1])).commit();
    }
}
