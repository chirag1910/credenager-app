package com.credenager.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.credenager.interfaces.DialogResponse;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.credenager.R;

public class ConfirmationDialog extends Dialog {
    private final DialogResponse dialogResponse;
    private final String headingText, buttonText;


    public ConfirmationDialog(@NonNull Context context, String headingText, String buttonText, DialogResponse dialogResponse) {
        super(context);

        this.headingText = headingText;
        this.buttonText = buttonText;
        this.dialogResponse = dialogResponse;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmation_dialog);
        Window window = getWindow();
        window.setBackgroundDrawable(null);
        setCanceledOnTouchOutside(true);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout dialog = findViewById(R.id.confirmation_dialog);
        TextView heading = findViewById(R.id.confirm_dialog_heading);
        ExtendedFloatingActionButton button = findViewById(R.id.confirm_dialog_button);

        heading.setText(headingText);
        button.setText(buttonText);

        dialog.setScaleX(0);
        dialog.setScaleY(0);
        dialog.setVisibility(View.VISIBLE);
        dialog.animate().scaleX(1).scaleY(1).setDuration(500).start();

        button.setOnClickListener(this::handleButtonClick);
    }


    private void handleButtonClick(View view) {
        dismiss();
        dialogResponse.onFinish(true);
    }
}
