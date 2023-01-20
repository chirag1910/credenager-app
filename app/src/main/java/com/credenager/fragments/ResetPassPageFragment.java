package com.credenager.fragments;

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
import com.credenager.utils.Globals;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.credenager.R;
import com.credenager.utils.Api;

public class ResetPassPageFragment extends Fragment {
    private EditText emailEdittext, otpEdittext, passwordEdittext;
    private ExtendedFloatingActionButton submitButton;
    private boolean otpSent = false;
    private final String emailAutoFill;

    public ResetPassPageFragment(String emailAutoFill){
        this.emailAutoFill = emailAutoFill;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.reset_pass_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emailEdittext = view.findViewById(R.id.reset_email_edittext);
        otpEdittext = view.findViewById(R.id.reset_otp_edittext);
        passwordEdittext = view.findViewById(R.id.reset_password_edittext);
        submitButton = view.findViewById(R.id.reset_submit_button);
        TextView loginInsteadLink = view.findViewById(R.id.reset_login_instead_link);
        float width = Resources.getSystem().getDisplayMetrics().widthPixels - Globals.dpToPx(40, requireContext());
        view.findViewById(R.id.header_image).getLayoutParams().width = width > 900 ? 900 : (int) width;

        submitButton.setOnClickListener(this::handleSubmit);
        loginInsteadLink.setOnClickListener(this::gotoLoginPage);

        emailEdittext.setText(emailAutoFill);
    }

    private void handleSubmit(View view) {
        final String email = emailEdittext.getText().toString();

        if (email.isEmpty()){
            Toast.makeText(getContext(), "Email Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }

        enableButtons(false);

        if(!otpSent){
            Api.resetPassInit(email, response -> {
                if (getContext() == null) return;

                try{
                    if (((Integer) response.get("code")) == 200) {
                        new Handler(Looper.getMainLooper()).post(this::setOtpSent);
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
        } else{
            int otp;
            final String password = passwordEdittext.getText().toString();

            try{
                otp = Integer.parseInt(otpEdittext.getText().toString());
            } catch (NumberFormatException e){
                Toast.makeText(getContext(), "OTP Is Required!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (String.valueOf(otp).isEmpty()){
                Toast.makeText(getContext(), "OTP Is Required!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()){
                Toast.makeText(getContext(), "Password Is Required!", Toast.LENGTH_SHORT).show();
                return;
            }

            Api.resetPass(email, otp, password, response -> {
                if (getContext() == null) return;

                try{
                    if (((Integer) response.get("code")) == 200) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(getContext(), "Password updated successfully!", Toast.LENGTH_LONG).show();
                            gotoLoginPage(view);
                        });
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
    }

    private void setOtpSent (){
        otpSent = true;
        emailEdittext.setEnabled(false);
        emailEdittext.setAlpha(0.3f);
        otpEdittext.setScaleX(0);
        otpEdittext.setScaleY(0);
        passwordEdittext.setScaleX(0);
        passwordEdittext.setScaleY(0);
        otpEdittext.setVisibility(View.VISIBLE);
        passwordEdittext.setVisibility(View.VISIBLE);
        otpEdittext.animate().scaleX(1).scaleY(1).start();
        passwordEdittext.animate().scaleX(1).scaleY(1).start();
        submitButton.setText(R.string.reset_password);
    }

    private void enableButtons (boolean enable){
        submitButton.setEnabled(enable);

        if (!enable)
            submitButton.setAlpha(0.3f);
        else
            submitButton.setAlpha(1);
    }

    private void gotoLoginPage(View view) {
        requireActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_from_right)
                .replace(R.id.fragment_container, new LoginPageFragment(), Globals.LOGIN_FRAGMENT_TAG).commit();
    }


}
