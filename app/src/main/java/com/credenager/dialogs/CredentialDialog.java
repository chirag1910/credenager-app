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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.credenager.R;
import com.credenager.interfaces.DialogResponse;
import com.credenager.utils.Api;
import com.credenager.utils.Crypt;
import com.credenager.utils.Data;
import com.credenager.utils.Globals;
import com.credenager.utils.Session;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.HashMap;

public class CredentialDialog extends Dialog {
    private final DialogResponse dialogResponse;
    private final String groupId;
    private String id, identifier, credential;
    private TextView heading;
    private ExtendedFloatingActionButton button;
    private EditText identifierEditText, credentialEditText;

    public CredentialDialog(@NonNull Context context, String groupId, DialogResponse dialogResponse) {
        super(context);
        this.groupId = groupId;
        this.dialogResponse = dialogResponse;
    }

    public CredentialDialog(@NonNull Context context, String id, String identifier, String credential, String groupId, DialogResponse dialogResponse) {
        super(context);

        this.id = id;
        this.identifier = identifier;
        this.credential = credential;
        this.groupId = groupId;
        this.dialogResponse = dialogResponse;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.credential_dialog);
        Window window = getWindow();
        window.setBackgroundDrawable(null);
        setCanceledOnTouchOutside(true);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout dialog = findViewById(R.id.credential_dialog);
        heading = findViewById(R.id.cred_dialog_heading);
        button = findViewById(R.id.cred_dialog_button);
        identifierEditText = findViewById(R.id.cred_dialog_identifier_edittext);
        credentialEditText = findViewById(R.id.cred_dialog_cred_edittext);

        dialog.animate().scaleX(1).scaleY(1).setDuration(500).start();

        setUi();
        focusEditText();
        button.setOnClickListener(this::handleSubmit);
    }

    private void focusEditText(){
        identifierEditText.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void setUi(){
        if(id == null){
            heading.setText(R.string.add_credential);
            button.setText("Add");
        }else{
            heading.setText("Edit Credential");
            identifierEditText.setText(identifier);
            credentialEditText.setText(credential);
            button.setText("Update");
        }
    }

    private void handleSubmit(View view){
        final String identifier = identifierEditText.getText().toString();
        final String value = credentialEditText.getText().toString();

        if (identifier.isEmpty()){
            Toast.makeText(getContext(), getContext().getString(R.string.cred_dialog_identifier) + " Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (value.isEmpty()){
            Toast.makeText(getContext(), "Credential Value Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }

        enableButtons(false);

        if (id == null){
            Api.addCred(Session.JWT_TOKEN, groupId, identifier, Crypt.encrypt(value, Session.USER_KEY), response -> {
                try{
                    if (((Integer) response.get("code")) == 200) {
                        String credId = response.getString("_id");
                        Data.addCred(credId, identifier, value, groupId);
                        Data.cachedCred = new HashMap<>();
                        new Handler(Looper.getMainLooper()).post(() -> {
                            dismiss();
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
                new Handler(Looper.getMainLooper()).post(()->enableButtons(true));
            });
        }else{
            Api.updateCred(Session.JWT_TOKEN, id, identifier, Crypt.encrypt(value, Session.USER_KEY), response -> {
                try{
                    if (((Integer) response.get("code")) == 200) {
                        Data.updateCred(id, identifier, value);
                        Data.cachedCred = new HashMap<>();
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(getContext(), "Updated", Toast.LENGTH_LONG).show();
                            dismiss();
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
                new Handler(Looper.getMainLooper()).post(()->enableButtons(true));
            });
        }
    }

    private void enableButtons(boolean enable){
        button.setEnabled(enable);
        setCancelable(enable);

        if (enable)
            button.setAlpha(1f);
        else
            button.setAlpha(0.3f);
    }
}
