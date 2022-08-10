package com.credenager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.credenager.R;
import com.credenager.fragments.GroupPageFragment;
import com.credenager.models.Group;
import com.credenager.utils.Data;
import com.credenager.utils.Globals;

public class GroupAdapter extends RecyclerView.Adapter<GroupItem> {
    private final Context context;

    public GroupAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public GroupItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupItem(LayoutInflater.from(context).inflate(R.layout.group_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupItem holder, int position) {
        Group group = Data.groups.get(position);
        holder.setName(group.getName());
        holder.setOnClick(context, group.getId());
    }

    @Override
    public int getItemCount() {
        return Data.groups.size();
    }
}

class GroupItem extends RecyclerView.ViewHolder {
    private final TextView nameTextview;
    private final View itemView;

    public GroupItem(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        nameTextview = itemView.findViewById(R.id.group_card_name);
    }

    public void setName(String name){
        nameTextview.setText(name);
    }

    public void setOnClick(Context context, String groupId){
        itemView.setOnClickListener(view ->
                ((FragmentActivity)context)
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right,R.anim.exit_from_left)
                        .replace(R.id.fragment_container, new GroupPageFragment(groupId), Globals.GROUP_FRAGMENT_TAG)
                        .commit()
        );
    }
}