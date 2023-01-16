package com.credenager.fragments;

import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.credenager.HomeActivity;
import com.credenager.utils.Globals;
import com.credenager.utils.Session;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.credenager.R;
import com.credenager.utils.Api;

public class LoginPageFragment extends Fragment {
    private EditText emailEdittext, passEdittext;
    private ExtendedFloatingActionButton submitButton, googleButton;
    private SignInClient oneTapClient;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView signupInsteadLink = view.findViewById(R.id.signup_instead_link);
        TextView forgotPassLink = view.findViewById(R.id.reset_pass_link);
        emailEdittext = view.findViewById(R.id.login_email_edittext);
        passEdittext = view.findViewById(R.id.login_pass_edittext);
        submitButton = view.findViewById(R.id.login_submit_button);
        googleButton = view.findViewById(R.id.login_google_button);
        float width = Resources.getSystem().getDisplayMetrics().widthPixels - Globals.dpToPx(40, requireContext());
        view.findViewById(R.id.header_image).getLayoutParams().width = width > 900 ? 900 : (int) width;

        signupInsteadLink.setOnClickListener(this::gotoSignupPage);
        forgotPassLink.setOnClickListener(this::gotoResetPassPage);
        submitButton.setOnClickListener(this::handleSubmit);
        googleButton.setOnClickListener(this::handleGoogle);

    }

    private void handleGoogle(View view) {
        enableButtons(false);
        oneTapClient = Identity.getSignInClient(requireActivity());
        BeginSignInRequest signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(Globals.GOOGLE_CLIENT_ID)
                        .build())
                .setAutoSelectEnabled(false)
                .build();
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(requireActivity(), result -> {
                    try {
                        startIntentSenderForResult(
                                result.getPendingIntent().getIntentSender(), Globals.GOOGLE_REQUEST_CODE,
                                null, 0, 0, 0, null);
                    } catch (IntentSender.SendIntentException e) {
                        new Handler(Looper.getMainLooper()).post(() -> {Toast.makeText(requireActivity(), "Couldn't connect to google!", Toast.LENGTH_SHORT).show();
                            enableButtons(true);
                        });
                    }
                })
                .addOnFailureListener(requireActivity(), e ->
                        new Handler(Looper.getMainLooper()).post(() -> {Toast.makeText(requireActivity(), "Google error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            enableButtons(true);
                        })
                );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Globals.GOOGLE_REQUEST_CODE) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) {
                    enableButtons(false);

                    Api.loginGoogle(idToken, response -> {
                        try {
                            if (((Integer) response.get("code")) == 200) {
                                final String token = response.getString("token");
                                final String email = response.getString("email");
                                Globals.saveUserState(requireContext(), email, token);
                                Session.setUserState(email, token);
                                new Handler(Looper.getMainLooper()).post(this::gotoKeyPage);
                            } else {
                                String error = response.getString("error");
                                new Handler(Looper.getMainLooper()).post(() ->
                                        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show()
                                );
                            }
                        } catch (Exception e) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    Toast.makeText(getContext(), "Unknown Error Occurred!", Toast.LENGTH_LONG).show()
                            );
                        }
                        new Handler(Looper.getMainLooper()).post(() -> enableButtons(true));
                    });
                }else{
                    throw new Exception();
                }
            } catch (Exception e) {
                enableButtons(true);
                Toast.makeText(requireActivity(), "Unknown Error Occurred", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handleSubmit(View view) {
        final String email = emailEdittext.getText().toString();
        final String password = passEdittext.getText().toString();

        if (email.isEmpty()){
            Toast.makeText(getContext(), "Email Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()){
            Toast.makeText(getContext(), "Password Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }

        enableButtons(false);

        Api.login(email, password, response -> {
            try{
                if (((Integer) response.get("code")) == 200) {
                    final String token =  response.getString("token");
                    Globals.saveUserState(requireContext(), email, token);
                    Session.setUserState(email, token);
                    new Handler(Looper.getMainLooper()).post(this::gotoKeyPage);
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

    private void enableButtons (boolean enable){
        submitButton.setEnabled(enable);
        googleButton.setEnabled(enable);

        if (!enable){
            submitButton.setAlpha(0.3f);
            googleButton.setAlpha(0.3f);
        }else{
            submitButton.setAlpha(1);
            googleButton.setAlpha(1);
        }
    }

    private void gotoResetPassPage(View view) {
        requireActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left)
                .replace(R.id.fragment_container, new ResetPassPageFragment(emailEdittext.getText().toString()), Globals.RESET_PASS_FRAGMENT_TAG).commit();
    }

    private void gotoKeyPage() {
        requireActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_from_left
        ).replace(R.id.fragment_container, new KeyPageFragment(-1, -1)
        ).commit();
    }

    private void gotoSignupPage(View view) {
        requireActivity().getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_container, new SignupPageFragment(), Globals.SIGNUP_FRAGMENT_TAG).commit();
    }
}
