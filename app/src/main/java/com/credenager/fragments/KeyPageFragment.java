package com.credenager.fragments;

import android.animation.Animator;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.credenager.HomeActivity;
import com.credenager.R;
import com.credenager.dialogs.ConfirmationDialog;
import com.credenager.utils.Api;
import com.credenager.utils.Crypt;
import com.credenager.utils.Data;
import com.credenager.utils.Globals;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class KeyPageFragment extends Fragment {
    private final float startX, startY;
    private EditText keyEdittext;
    private ExtendedFloatingActionButton validateButton;
    private TextView emailIndicator, forgotKeyLink, logoutLink;

    public KeyPageFragment(float x, float y) {
        startX = x;
        startY = y;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.key_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View keyTop = view.findViewById(R.id.key_top);
        emailIndicator = view.findViewById(R.id.key_email_indicator);
        keyEdittext = view.findViewById(R.id.key_key_edittext);
        validateButton = view.findViewById(R.id.key_validate_button);
        forgotKeyLink = view.findViewById(R.id.forgot_key_link);
        logoutLink = view.findViewById(R.id.key_logout_link);
        float width = Resources.getSystem().getDisplayMetrics().widthPixels - Globals.dpToPx(40, requireContext());
        view.findViewById(R.id.header_image).getLayoutParams().width = width > 900 ? 900 : (int) width;

        emailIndicator.setText(
                (Globals.APP_OFFLINE_MODE && Globals.USER_EMAIL == null)
                        ? "App Running In Offline Mode"
                        : "Logged in as ".concat(Globals.USER_EMAIL)
        );

        if (startY >= 0 && startX >= 0) {
            int[] coordinates = new int[2];
            keyTop.getLocationOnScreen(coordinates);
            keyTop.setTranslationY((startY - Globals.dpToPx(70, requireContext())) - coordinates[1]);
            keyTop.setTranslationX(startX - coordinates[0]);
            keyTop.setVisibility(View.VISIBLE);
            keyTop.animate().setInterpolator(new DecelerateInterpolator()).translationY(0).translationX(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    emailIndicator.setAlpha(0);
                    emailIndicator.setVisibility(View.VISIBLE);
                    keyEdittext.setScaleX(0);
                    keyEdittext.setScaleY(0);
                    keyEdittext.setVisibility(View.VISIBLE);
                    validateButton.setScaleX(0);
                    validateButton.setScaleY(0);
                    validateButton.setVisibility(View.VISIBLE);
                    forgotKeyLink.setAlpha(0);
                    forgotKeyLink.setVisibility(View.VISIBLE);
                    logoutLink.setAlpha(0);
                    logoutLink.setVisibility(View.VISIBLE);
                    emailIndicator.animate().alpha(1).start();
                    keyEdittext.animate().scaleX(1).scaleY(1).start();
                    validateButton.animate().scaleX(1).scaleY(1).start();
                    forgotKeyLink.animate().alpha(1).start();
                    logoutLink.animate().alpha(1).start();
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
            keyTop.setVisibility(View.VISIBLE);
            emailIndicator.setVisibility(View.VISIBLE);
            keyEdittext.setVisibility(View.VISIBLE);
            validateButton.setVisibility(View.VISIBLE);
            forgotKeyLink.setVisibility(View.VISIBLE);
            logoutLink.setVisibility(View.VISIBLE);
        }

        validateButton.setOnClickListener(this::handleSubmit);
        logoutLink.setOnClickListener(this::handleLogout);
        forgotKeyLink.setOnClickListener(this::gotoResetKeyPage);
    }

    private void handleSubmit(View view) {
        final String key = keyEdittext.getText().toString();

        if (key.isEmpty()){
            Toast.makeText(getContext(), "Key Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }

        enableButtons(false);

        Api.verifyKey(Globals.JWT_TOKEN, key, response -> {
            try{
                int responseCode = response.getInt("code");
                if (responseCode == 200) {
                    Globals.KEY = key;
                    if (Globals.getOfflineSetting(requireContext())) {
                        Globals.setKey(requireContext(), Crypt.encrypt(Globals.KEY, Globals.KEY));
                    }
                    else {
                        Globals.setKey(requireContext(), null);
                    }
                    new Handler(Looper.getMainLooper()).post(this::gotoHomePage);
                }
                else if (responseCode == 502) {
                    String storedKey = Globals.getKey(requireContext());

                    if (Globals.APP_OFFLINE_MODE) {
                        if (Crypt.decrypt(storedKey, key).equals(key)) {
                            Globals.KEY = key;
                            new Handler(Looper.getMainLooper()).post(this::gotoHomePage);
                        }
                        else {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    Toast.makeText(getContext(), "Invalid Key", Toast.LENGTH_LONG).show()
                            );
                        }
                    }
                    else{
                        String error = response.getString("error");
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show()
                        );
                    }
                }
                else{
                    String error = response.getString("error");
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show()
                    );
                }
            } catch (Exception e){
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(getContext(), "Unknown Error Occurred!", Toast.LENGTH_LONG).show()
                );
            }
            new Handler(Looper.getMainLooper()).post(() -> enableButtons(true));
        });
    }

    private void handleLogout(View view) {
        new ConfirmationDialog(requireContext(), "Logout?", "Logout", result -> {
            if (result) {
                Globals.setToken(requireContext(), null);
                Globals.setUserState(null, null);
                Globals.KEY = null;
                Globals.setKey(requireContext(), null);
                Data.dataString = null;
                Globals.setData(requireContext(), null);
                gotoLoginPage();
            }
        }).show();
    }

    private void enableButtons (boolean enable){
        validateButton.setEnabled(enable);

        if (!enable)
            validateButton.setAlpha(0.3f);
        else
            validateButton.setAlpha(1);
    }

    private void gotoHomePage() {
        requireActivity().startActivity(new Intent(requireActivity(), HomeActivity.class));
        requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_from_left);
        requireActivity().finish();
    }

    private void gotoLoginPage() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_from_right)
                .replace(R.id.fragment_container, new LoginPageFragment(), Globals.LOGIN_FRAGMENT_TAG)
                .commit();
    }

    private void gotoResetKeyPage(View view) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left)
                .replace(R.id.fragment_container, new ResetKeyPageFragment(), Globals.RESET_KEY_FRAGMENT_TAG)
                .commit();
    }
}
