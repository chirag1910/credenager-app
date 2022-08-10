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
import com.credenager.utils.Data;
import com.credenager.utils.Globals;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class GroupDialog extends Dialog {
    private final DialogResponse dialogResponse;
    private String id, name;
    private TextView heading;
    private EditText nameEditText;
    private ExtendedFloatingActionButton button;

    public GroupDialog(@NonNull Context context, DialogResponse dialogResponse) {
        super(context);
        this.dialogResponse = dialogResponse;
    }

    public GroupDialog(@NonNull Context context, String id, String name, DialogResponse dialogResponse) {
        super(context);

        this.id = id;
        this.name = name;
        this.dialogResponse = dialogResponse;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_dialog);
        Window window = getWindow();
        window.setBackgroundDrawable(null);
        setCanceledOnTouchOutside(true);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout dialog = findViewById(R.id.group_dialog);
        heading = findViewById(R.id.group_dialog_heading);
        button = findViewById(R.id.group_dialog_button);
        nameEditText = findViewById(R.id.group_dialog_name_edittext);

        dialog.animate().scaleX(1).scaleY(1).setDuration(500).start();

        setUi();
        focusEditText();
        button.setOnClickListener(this::handleSubmit);
    }

    private void focusEditText(){
        nameEditText.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void setUi(){
        if(id == null){
            heading.setText(R.string.add_group);
            button.setText("Add");
        }else{
            heading.setText("Edit Group");
            nameEditText.setText(name);
            button.setText("Update");
        }
    }

    private void handleSubmit(View view){
        final String name = nameEditText.getText().toString();

        if (name.isEmpty()){
            Toast.makeText(getContext(), getContext().getString(R.string.group_name) + " Is Required!", Toast.LENGTH_SHORT).show();
            return;
        }

        enableButtons(false);

        if (id == null){
            Api.addGroup(Globals.JWT_TOKEN, name, response -> {
                try{
                    if (((Integer) response.get("code")) == 200) {
                        String groupId = response.getString("_id");
                        Data.addGroup(groupId, name);
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
            Api.updateGroup(Globals.JWT_TOKEN, id, name, response -> {
                try{
                    if (((Integer) response.get("code")) == 200) {
                        Data.updateGroup(id, name);
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
