package com.credenager.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.credenager.R;
import com.credenager.interfaces.DialogResponse;
import com.credenager.utils.Api;
import com.credenager.utils.Data;
import com.credenager.utils.Globals;
import com.credenager.utils.Session;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class DeleteAccountDialog extends Dialog {
    private final DialogResponse dialogResponse;
    private EditText passEditText, keyEditText;
    private CheckBox confirmationCheckbox;
    private ExtendedFloatingActionButton button;

    public DeleteAccountDialog(@NonNull Context context, DialogResponse dialogResponse) {
        super(context);
        this.dialogResponse = dialogResponse;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_acc_dialog);
        Window window = getWindow();
        window.setBackgroundDrawable(null);
        setCanceledOnTouchOutside(true);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout dialog = findViewById(R.id.delete_acc_dialog);
        button = findViewById(R.id.delete_acc_dialog_button);
        passEditText = findViewById(R.id.delete_acc_dialog_pass_edittext);
        keyEditText = findViewById(R.id.delete_acc_dialog_key_edittext);
        confirmationCheckbox = findViewById(R.id.delete_acc_confirm_checkbox);

        dialog.animate().scaleX(1).scaleY(1).setDuration(500).start();

        focusEditText();
        confirmationCheckbox.setOnClickListener(this::handleCheckboxClick);
        button.setOnClickListener(this::handleSubmit);
    }

    private void focusEditText(){
        passEditText.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void handleSubmit(View view){
        final String pass = passEditText.getText().toString();
        final String key = keyEditText.getText().toString();

        if (pass.isEmpty()){
            Toast.makeText(getContext(), getContext().getString(R.string.password_edittext_hint) + " Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (key.isEmpty()){
            Toast.makeText(getContext(), getContext().getString(R.string.key_edittext_hint) + " Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }

        enableButtons(false, false);

        Api.deleteAccount(Session.JWT_TOKEN, pass, key, response -> {
            if (getContext() == null) return;

            try{
                if (((Integer) response.get("code")) == 200) {
                    Globals.logout(getContext());
                    new Handler(Looper.getMainLooper()).post(() -> {
                        dismiss();
                        Toast.makeText(getContext(), "Account Deleted!", Toast.LENGTH_LONG).show();
                        dialogResponse.onFinish(true);
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
            new Handler(Looper.getMainLooper()).post(()-> enableButtons(true, true));
        });

    }

    private void handleCheckboxClick(View view) {
        enableButtons(((CheckBox) view).isChecked(), true);
    }

    private void enableButtons (boolean enableButton, boolean enableCheckbox){
        button.setEnabled(enableButton);
        confirmationCheckbox.setEnabled(enableCheckbox);
        setCancelable(enableCheckbox);

        if (!enableButton)
            button.setAlpha(0.3f);
        else
            button.setAlpha(1);
    }
}
