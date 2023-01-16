package com.credenager.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.credenager.MainActivity;
import com.credenager.R;
import com.credenager.dialogs.ChangePasswordDialog;
import com.credenager.dialogs.ConfirmationDialog;
import com.credenager.dialogs.DeleteAccountDialog;
import com.credenager.utils.Data;
import com.credenager.utils.Globals;

import java.util.HashMap;

public class SettingsPageFragment extends Fragment {
    private LinearLayout offlineTile, bypassKeyTile, biometricTile;
    private Switch offlineToggle, bypassKeyToggle, biometricToggle;
    private Boolean offlineMode, bypassKey, biometric;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.settings_toolbar);
        offlineToggle = view.findViewById(R.id.settings_offline_toggle);
        offlineTile = view.findViewById(R.id.settings_offline_tile);
        bypassKeyToggle = view.findViewById(R.id.settings_bypass_key_toggle);
        bypassKeyTile = view.findViewById(R.id.settings_bypass_key_tile);
        biometricToggle = view.findViewById(R.id.settings_biometric_toggle);
        biometricTile = view.findViewById(R.id.settings_biometric_tile);
        LinearLayout changeTile = view.findViewById(R.id.settings_reset_tile);
        LinearLayout deleteTile = view.findViewById(R.id.settings_delete_account_tile);
        LinearLayout logoutTile = view.findViewById(R.id.settings_logout_tile);
        LinearLayout githubTile = view.findViewById(R.id.settings_github_tile);

        requireActivity().setActionBar(toolbar);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().getActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left);

        offlineTile.setOnClickListener(this::offlineModeClick);
        bypassKeyTile.setOnClickListener(this::bypassKeyClick);
        changeTile.setOnClickListener(this::changePassClick);
        deleteTile.setOnClickListener(this::deleteAccountClick);
        logoutTile.setOnClickListener(this::logoutClick);
        githubTile.setOnClickListener(this::githubClick);

        handleBiometricInit();
        loadSettings();
    }

    private void loadSettings() {
        HashMap<String, Object> settings = Globals.getSettings(requireContext());
        offlineMode = (Boolean) settings.getOrDefault(Globals.OFFLINE_KEY, false);
        offlineToggle.setChecked(Boolean.TRUE.equals(offlineMode));
        biometric = (Boolean) settings.getOrDefault(Globals.BIOMETRIC_KEY, false);
        biometricToggle.setChecked(Boolean.TRUE.equals(biometric));
        bypassKey = (Boolean) settings.getOrDefault(Globals.BYPASS_KEY_KEY, false);
        bypassKeyToggle.setChecked(Boolean.TRUE.equals(bypassKey));
    }

    private void handleBiometricInit() {
        int biometricStatus = Globals.getBiometricStatus(requireContext());
        if(biometricStatus == 0 || biometricStatus == 1) {
            biometricTile.setVisibility(View.VISIBLE);
            biometricTile.setOnClickListener((view) -> biometricClick(biometricStatus));
        }
        if (biometricStatus == 0){
            biometricTile.setAlpha(0.5f);
        }
        if (biometricStatus != 1) {
            Globals.saveSettings(requireContext(), null, null, false);
        }
    }

    private void offlineModeClick(View view) {
        offlineTile.setEnabled(false);
        offlineMode = !offlineMode;

        if (offlineMode) {
            Globals.saveData(requireContext(), Data.dataString);
        }
        else {
            Globals.saveData(requireContext(), null);
        }

        Globals.saveSettings(requireContext(), offlineMode, null, null);
        offlineToggle.setChecked(offlineMode);

        offlineTile.setEnabled(true);
    }

    private void biometricClick(int biometricStatus){
        if (biometricStatus == 1){
            biometricTile.setEnabled(false);
            biometric = !biometric;

            Globals.saveSettings(requireContext(), null, null, biometric);
            biometricToggle.setChecked(biometric);

            biometricTile.setEnabled(true);
        } else{
            Toast.makeText(requireContext(), "No Biometric is registered on the device!", Toast.LENGTH_SHORT).show();
        }
    }

    private void bypassKeyClick(View view) {
        bypassKeyTile.setEnabled(false);
        bypassKey = !bypassKey;

        Globals.saveSettings(requireContext(), null, bypassKey, null);
        bypassKeyToggle.setChecked(bypassKey);

        bypassKeyTile.setEnabled(true);
    }

    private void changePassClick(View view) {
        new ChangePasswordDialog(requireContext(), result -> {}).show();
    }

    private void deleteAccountClick(View view) {
        new DeleteAccountDialog(requireContext(), result -> goToMainActivity()).show();
    }

    private void logoutClick(View view) {
        new ConfirmationDialog(requireContext(), "Logout?", "Logout", result -> {
            if (result){
                Globals.logout(requireContext());
                goToMainActivity();
            }
        }).show();
    }

    private void githubClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/chirag1910/credenagerApp.git"));
        startActivity(browserIntent);
    }

    private void goToMainActivity() {
        requireActivity().startActivity(new Intent(requireActivity(), MainActivity.class));
        requireActivity().finish();
        requireActivity().overridePendingTransition(R.anim.enter_from_left, R.anim.exit_from_right);
    }

}
