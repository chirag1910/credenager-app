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
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.credenager.R;
import com.credenager.interfaces.DialogResponse;
import com.credenager.utils.Api;
import com.credenager.utils.Globals;
import com.credenager.utils.Session;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class ChangePasswordDialog extends Dialog {
    private final DialogResponse dialogResponse;
    private EditText currPassEditText, newPassEditText;
    private ExtendedFloatingActionButton button;

    public ChangePasswordDialog(@NonNull Context context, DialogResponse dialogResponse) {
        super(context);
        this.dialogResponse = dialogResponse;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_pass_dialog);
        Window window = getWindow();
        window.setBackgroundDrawable(null);
        setCanceledOnTouchOutside(true);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout dialog = findViewById(R.id.change_pass_dialog);
        button = findViewById(R.id.change_pass_dialog_button);
        currPassEditText = findViewById(R.id.change_pass_dialog_current_edittext);
        newPassEditText = findViewById(R.id.change_pass_dialog_new_edittext);

        dialog.animate().scaleX(1).scaleY(1).setDuration(500).start();

        focusEditText();
        button.setOnClickListener(this::handleSubmit);
    }

    private void focusEditText(){
        currPassEditText.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void handleSubmit(View view){
        final String oldPass = currPassEditText.getText().toString();
        final String newPass = newPassEditText.getText().toString();

        if (oldPass.isEmpty()){
            Toast.makeText(getContext(), getContext().getString(R.string.current_password_edittext_hint) + " Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPass.isEmpty()){
            Toast.makeText(getContext(), getContext().getString(R.string.new_password_edittext_hint) + " Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }

        enableButtons(false);
        Api.changePassword(Session.JWT_TOKEN, oldPass, newPass, response -> {
            try{
                if (((Integer) response.get("code")) == 200) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        dismiss();
                        Toast.makeText(getContext(), "Password has been updated!", Toast.LENGTH_LONG).show();
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

    private void enableButtons(boolean enable){
        button.setEnabled(enable);
        setCancelable(enable);

        if (enable)
            button.setAlpha(1f);
        else
            button.setAlpha(0.3f);
    }
}
