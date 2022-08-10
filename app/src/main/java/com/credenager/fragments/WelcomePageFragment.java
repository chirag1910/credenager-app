package com.credenager.fragments;

import android.animation.Animator;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.credenager.R;
import com.credenager.utils.Globals;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class WelcomePageFragment extends Fragment {
    private final int startX, startY;
    private ExtendedFloatingActionButton registerButton, loginButton;

    public WelcomePageFragment(int x, int y) {
        startX = x;
        startY = y;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.welcome_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View welcomeTop = view.findViewById(R.id.welcome_top);
        registerButton = view.findViewById(R.id.register_button);
        loginButton = view.findViewById(R.id.login_button);
        float width = Resources.getSystem().getDisplayMetrics().widthPixels - Globals.dpToPx(40, requireContext());
        view.findViewById(R.id.header_image).getLayoutParams().width = width > 900 ? 900 : (int) width;

        if (startY >= 0 && startX >= 0) {
            int[] coordinates = new int[2];
            welcomeTop.getLocationOnScreen(coordinates);
            welcomeTop.setTranslationY((startY - Globals.dpToPx(70, requireContext())) - coordinates[1]);
            welcomeTop.setTranslationX(startX - coordinates[0]);
            welcomeTop.setVisibility(View.VISIBLE);
            welcomeTop.animate().setInterpolator(new DecelerateInterpolator()).translationY(0).translationX(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    registerButton.setScaleX(0);
                    registerButton.setScaleY(0);
                    registerButton.setVisibility(View.VISIBLE);
                    loginButton.setScaleX(0);
                    loginButton.setScaleY(0);
                    loginButton.setVisibility(View.VISIBLE);
                    registerButton.animate().scaleX(1).scaleY(1).start();
                    loginButton.animate().scaleX(1).scaleY(1).start();
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            }).start();

        }
        else {
            registerButton.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.VISIBLE);
            welcomeTop.setVisibility(View.VISIBLE);
        }

        loginButton.setOnClickListener(this::gotoLoginPage);
        registerButton.setOnClickListener(this::gotoSignupPage);
    }

    private void gotoSignupPage(View view) {
        requireActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left)
                .replace(R.id.fragment_container, new SignupPageFragment(), Globals.SIGNUP_FRAGMENT_TAG).commit();
    }

    private void gotoLoginPage(View view) {
        requireActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left)
                .replace(R.id.fragment_container, new LoginPageFragment(), Globals.LOGIN_FRAGMENT_TAG).commit();
    }
}
