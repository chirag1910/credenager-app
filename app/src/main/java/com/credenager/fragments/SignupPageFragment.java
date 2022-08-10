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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.credenager.HomeActivity;
import com.credenager.utils.Crypt;
import com.google.android.gms.auth.api.identity.*;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.credenager.R;
import com.credenager.utils.Api;
import com.credenager.utils.Globals;

public class SignupPageFragment extends Fragment {
    private CheckBox keySameAsPass;
    private EditText emailEdittext, passEdittext, keyEdittext, key2Edittext;
    private ExtendedFloatingActionButton submitButton, googleButton;
    private SignInClient oneTapClient;
    private String key_google;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.signup_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView loginInsteadLink = view.findViewById(R.id.login_instead_link);
        emailEdittext = view.findViewById(R.id.signup_email_edittext);
        passEdittext = view.findViewById(R.id.signup_password_edittext);
        keyEdittext = view.findViewById(R.id.signup_key_edittext);
        keySameAsPass = view.findViewById(R.id.key_same_as_pass_checkbox);
        key2Edittext = view.findViewById(R.id.signup_key2_edittext);
        submitButton = view.findViewById(R.id.signup_submit_button);
        googleButton = view.findViewById(R.id.signup_google_button);
        float width = Resources.getSystem().getDisplayMetrics().widthPixels - Globals.dpToPx(40, requireContext());
        view.findViewById(R.id.header_image).getLayoutParams().width = width > 900 ? 900 : (int) width;

        loginInsteadLink.setOnClickListener(this::gotoLoginPage);
        keySameAsPass.setOnClickListener(this::handleCheckboxClick);
        submitButton.setOnClickListener(this::handleSubmit);
        googleButton.setOnClickListener(this::handleGoogle);
    }

    private void handleGoogle(View view) {
        key_google = key2Edittext.getText().toString();

        if (key_google.isEmpty()){
            Toast.makeText(getContext(), "Key Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }
        enableButtons(false);
        oneTapClient = Identity.getSignInClient(requireActivity());
        BeginSignInRequest signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId("274800871089-k8rdu64cfu1uq1n8hcf9tndldmnqhl27.apps.googleusercontent.com")
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

                    Api.signupGoogle(key_google, idToken, response -> {
                        try {
                            if (((Integer) response.get("code")) == 200) {
                                final String token = response.getString("token");
                                final String email = response.getString("email");
                                Globals.setToken(requireContext(), token);
                                Globals.setUserState(email, token);
                                Globals.KEY = key_google;
                                if (Globals.getOfflineSetting(requireContext())) {
                                    Globals.setKey(requireContext(), Crypt.encrypt(Globals.KEY, Globals.KEY));
                                }
                                else {
                                    Globals.setKey(requireContext(), null);
                                }
                                new Handler(Looper.getMainLooper()).post(this::gotoHomePage);
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
        final String key;
        if (keySameAsPass.isChecked())
            key = password;
        else
            key = keyEdittext.getText().toString();

        if (email.isEmpty()){
            Toast.makeText(getContext(), "Email Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Globals.isValidEmail(email)){
            Toast.makeText(getContext(), "Invalid Email Address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()){
            Toast.makeText(getContext(), "Password Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 8){
            Toast.makeText(getContext(), "Minimum Password Length Should Be 8!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() > 40){
            Toast.makeText(getContext(), "Maximum Password Length Should Be 40!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (key.isEmpty()){
            Toast.makeText(getContext(), "Key Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }

        enableButtons(false);

        Api.signup(email, password, key, response -> {
            try{
                if (((Integer) response.get("code")) == 200) {
                    final String token =  response.getString("token");
                    Globals.setToken(requireContext(), token);
                    Globals.setUserState(email, token);
                    Globals.KEY = key;
                    if (Globals.getOfflineSetting(requireContext())) {
                        Globals.setKey(requireContext(), Crypt.encrypt(Globals.KEY, Globals.KEY));
                    }
                    else {
                        Globals.setKey(requireContext(), null);
                    }
                    new Handler(Looper.getMainLooper()).post(this::gotoHomePage);
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

    private void handleCheckboxClick(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        keyEdittext.setEnabled(!checked);
        if (checked)
            keyEdittext.setAlpha(0.3f);
        else
            keyEdittext.setAlpha(1);
    }

    private void gotoHomePage() {
        requireActivity().startActivity(new Intent(requireActivity(), HomeActivity.class));
        requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_from_left);
        requireActivity().finish();
    }

    private void gotoLoginPage(View view) {
        requireActivity().getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_container, new LoginPageFragment(), Globals.LOGIN_FRAGMENT_TAG).commit();
    }
}
