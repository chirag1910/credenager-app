package com.credenager.fragments;

import android.app.appsearch.GlobalSearchSession;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.credenager.utils.Globals;
import com.credenager.utils.Session;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.credenager.R;
import com.credenager.utils.Api;

public class ResetKeyPageFragment extends Fragment {

    private EditText passwordEdittext, keyEdittext;
    private CheckBox confirmationCheckbox;
    private ExtendedFloatingActionButton submitButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.reset_key_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        passwordEdittext = view.findViewById(R.id.reset_key_password_edittext);
        keyEdittext = view.findViewById(R.id.reset_key_key_edittext);
        confirmationCheckbox = view.findViewById(R.id.reset_key_confirm_checkbox);
        submitButton = view.findViewById(R.id.reset_key_dialog_button);
        float width = Resources.getSystem().getDisplayMetrics().widthPixels - Globals.dpToPx(40, requireContext());
        view.findViewById(R.id.header_image).getLayoutParams().width = width > 900 ? 900 : (int) width;

        confirmationCheckbox.setOnClickListener(this::handleCheckboxClick);
        submitButton.setOnClickListener(this::handleSubmit);
    }

    private void handleSubmit(View view) {
        final String password = passwordEdittext.getText().toString();
        final String key = keyEdittext.getText().toString();

        if (password.isEmpty()){
            Toast.makeText(getContext(), "Password Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (key.isEmpty()){
            Toast.makeText(getContext(), "Key Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }

        enableButtons(false, false);

        Api.resetKey(Session.JWT_TOKEN, password, key, response -> {
            if (getContext() == null) return;

            try{
                if (((Integer) response.get("code")) == 200) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            {
                                Toast.makeText(getContext(), "Key Changed Successfully!", Toast.LENGTH_LONG).show();
                                Globals.saveKey(requireContext(), key);
                                gotoKeyPage();
                            }
                    );
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
            new Handler(Looper.getMainLooper()).post(() -> enableButtons(true, true));
        });
    }

    private void handleCheckboxClick(View view) {
        enableButtons(((CheckBox) view).isChecked(), true);
    }

    private void enableButtons (boolean enableButton, boolean enableCheckbox){
        submitButton.setEnabled(enableButton);
        confirmationCheckbox.setEnabled(enableCheckbox);

        if (!enableButton)
            submitButton.setAlpha(0.3f);
        else
            submitButton.setAlpha(1);
    }

    private void gotoKeyPage() {
        requireActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_from_right)
                .replace(R.id.fragment_container, new KeyPageFragment(-1, -1)).commit();
    }
}
