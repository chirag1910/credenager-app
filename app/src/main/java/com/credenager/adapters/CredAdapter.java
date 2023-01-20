package com.credenager.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.credenager.R;
import com.credenager.dialogs.ConfirmationDialog;
import com.credenager.dialogs.CredentialDialog;
import com.credenager.interfaces.AdapterResponse;
import com.credenager.models.Credential;
import com.credenager.utils.Api;
import com.credenager.utils.Data;
import com.credenager.utils.Globals;
import com.credenager.utils.Session;

import java.util.HashMap;

public class CredAdapter extends RecyclerView.Adapter<CredItem> {
    private final Context context;
    private final String groupId;
    private final boolean endPadding;
    private final AdapterResponse adapterResponse;


    public CredAdapter(Context context, String groupId, boolean endPadding, AdapterResponse adapterResponse) {
        this.context = context;
        this.groupId = groupId;
        this.endPadding = endPadding;
        this.adapterResponse = adapterResponse;
    }

    @NonNull
    @Override
    public CredItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CredItem(LayoutInflater.from(context).inflate(R.layout.cred_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CredItem holder, int position) {
        if (Data.cachedCred.get(groupId) == null) {
            Data.cachedCred.put(groupId, Data.getCredentialsByGroupId(groupId));
        }
        Credential credential = Data.cachedCred.get(groupId).get(position);
        holder.setIdentifier(credential.getIdentifier());
        holder.setValue(credential.getValue());
        holder.setEditListener(credential, () -> {
            notifyItemChanged(position);
            adapterResponse.onUpdate();
        });
        holder.setDeleteListener(credential, () -> {
            if (Data.cachedCred.get(groupId) == null) {
                Data.cachedCred.put(groupId, Data.getCredentialsByGroupId(groupId));
            }
            notifyItemRangeChanged((position != 0 ? position - 1 : position), Data.cachedCred.get(groupId).size() - position + 2);
            adapterResponse.onUpdate();
        });
        holder.setCopyListener(credential.getIdentifier(), credential.getValue());
        holder.setLockListener(credential.getValue());
        holder.setPadding(endPadding && (position == getItemCount()-1));
    }

    @Override
    public int getItemCount() {
        if (Data.cachedCred.get(groupId) == null) {
            Data.cachedCred.put(groupId, Data.getCredentialsByGroupId(groupId));
        }
        return Data.cachedCred.get(groupId).size();
    }
}

class CredItem extends RecyclerView.ViewHolder{
    private final View itemView, paddingView;
    private final ImageButton editButton, lockButton, deleteButton, copyButton;
    private final TextView identifierTextview, valueTextview;
    private boolean locked = true;
    public CredItem(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        identifierTextview = itemView.findViewById(R.id.cred_card_identifier);
        valueTextview = itemView.findViewById(R.id.cred_card_value);
        copyButton = itemView.findViewById(R.id.cred_card_copy);
        deleteButton = itemView.findViewById(R.id.cred_card_delete);
        lockButton = itemView.findViewById(R.id.cred_card_lock);
        editButton = itemView.findViewById(R.id.cred_card_edit);
        paddingView = itemView.findViewById(R.id.cred_card_bottom_padding);
    }


    public void setPadding(boolean enabled) {
        paddingView.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    public void setIdentifier(String identifier){
        identifierTextview.setText(identifier);
    }

    public void setValue(String value){
        if (locked)
            valueTextview.setText(value.replaceAll(".", "*"));
        else
            valueTextview.setText(value);
    }

    public void setEditListener(Credential credential, AdapterResponse adapterResponse){
        editButton.setOnClickListener(view -> {
            new CredentialDialog(
                    itemView.getContext(),
                    credential.getCredId(),
                    credential.getIdentifier(),
                    credential.getValue(),
                    credential.getGroupId(),
                    result -> {
                        if (result){
                            adapterResponse.onUpdate();
                        }
                    }
            ).show();
        });
    }

    public void setDeleteListener(Credential credential, AdapterResponse adapterResponse){
        deleteButton.setOnClickListener(view ->
                new ConfirmationDialog(
                        itemView.getContext(),
                        "Are You Sure?",
                        "Delete",
                        result ->
                        {
                            if (result) {
                                enableButtons(false);

                                Api.deleteCred(Session.JWT_TOKEN, credential.getCredId(), response -> {
                                    if (itemView.getContext() == null) return;
                                    
                                    try {
                                        if (((Integer) response.get("code")) == 200) {
                                            Data.deleteCred(credential.getCredId());
                                            Data.cachedCred = new HashMap<>();
                                            new Handler(Looper.getMainLooper()).post(adapterResponse::onUpdate);
                                        } else {
                                            String error = response.getString("error");
                                            new Handler(Looper.getMainLooper()).post(() ->
                                                    Toast.makeText(itemView.getContext(), error, Toast.LENGTH_LONG).show()
                                            );
                                        }
                                    } catch (Exception e) {
                                        new Handler(Looper.getMainLooper()).post(() ->
                                                Toast.makeText(itemView.getContext(), "Unknown Error Occurred!", Toast.LENGTH_LONG).show()
                                        );
                                    }

                                    new Handler(Looper.getMainLooper()).post(() -> enableButtons(true));
                                });
                            }
                        }
                ).show()
        );
    }

    public void setCopyListener(String identifier, String value){
        copyButton.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(identifier, value);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(itemView.getContext(), "Copied!", Toast.LENGTH_LONG).show();
        });
    }

    public void setLockListener(String value){
        lockButton.setOnClickListener(view -> {
            if (locked)
                lockButton.setImageDrawable(itemView.getResources().getDrawable(R.drawable.ic_hide));
            else
                lockButton.setImageDrawable(itemView.getResources().getDrawable(R.drawable.ic_show));

            locked = !locked;
            setValue(value);
        });
    }

    private void enableButtons(boolean enable){
        lockButton.setEnabled(enable);
        copyButton.setEnabled(enable);
        deleteButton.setEnabled(enable);
        editButton.setEnabled(enable);

        if (!enable) {
            lockButton.setAlpha(0.3f);
            copyButton.setAlpha(0.3f);
            deleteButton.setAlpha(0.3f);
            editButton.setAlpha(0.3f);
        }
        else {
            lockButton.setAlpha(1f);
            copyButton.setAlpha(1f);
            deleteButton.setAlpha(1f);
            editButton.setAlpha(1f);
        }
    }
}
