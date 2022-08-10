package com.credenager.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.credenager.R;
import com.credenager.fragments.GroupPageFragment;
import com.credenager.models.Credential;
import com.credenager.utils.Data;
import com.credenager.utils.Globals;

import java.util.List;

public class CredSearchAdapter extends RecyclerView.Adapter<CredSearchItem> {
    private final Context context;
    private final List<Credential> credentials;

    public CredSearchAdapter(Context context, List<Credential> credentials) {
        this.context = context;
        this.credentials = credentials;
    }

    @NonNull
    @Override
    public CredSearchItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CredSearchItem(LayoutInflater.from(context).inflate(R.layout.cred_search_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CredSearchItem holder, int position) {
        Credential credential = credentials.get(position);
        holder.setIdentifier(credential.getGroupId(), credential.getIdentifier());
        holder.setValue(credential.getValue());
        holder.setCopyListener(credential.getIdentifier(), credential.getValue());
        holder.setLockListener(credential.getValue());
        holder.setGotoListener(credential);
    }

    @Override
    public int getItemCount() {
        return credentials.size();
    }
}

class CredSearchItem extends RecyclerView.ViewHolder{
    private final View itemView;
    private final ImageButton gotoButton, lockButton, copyButton;
    private final TextView identifierTextview, valueTextview;
    private boolean locked = true;
    public CredSearchItem(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        identifierTextview = itemView.findViewById(R.id.cred_search_card_identifier);
        valueTextview = itemView.findViewById(R.id.cred_search_card_value);
        copyButton = itemView.findViewById(R.id.cred_search_card_copy);
        lockButton = itemView.findViewById(R.id.cred_search_card_lock);
        gotoButton = itemView.findViewById(R.id.cred_search_card_goto);
    }

    public void setIdentifier(String groupId, String identifier){
        if (groupId == null) {
            identifierTextview.setText(identifier);
        } else {
            String groupName = Data.getGroupById(groupId).getName();
            identifierTextview.setText(groupName.concat(": ").concat(identifier));
        }
    }

    public void setValue(String value){
        if (locked)
            valueTextview.setText(value.replaceAll(".", "*"));
        else
            valueTextview.setText(value);
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

    public void setGotoListener(Credential credential){

        gotoButton.setOnClickListener(view -> {
            Globals.hideKeyboard((FragmentActivity) view.getContext());
            ((FragmentActivity) itemView.getContext())
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left)
                    .hide(((FragmentActivity) itemView.getContext()).getSupportFragmentManager().findFragmentByTag(Globals.SEARCH_FRAGMENT_TAG))
                    .add(R.id.fragment_container,
                            new GroupPageFragment(
                                    credential.getGroupId(),
                                    credential.getCredId()
                            ),
                            Globals.FROM_SEARCH_FRAGMENT_TAG)
                    .commit();
        });
    }
}

