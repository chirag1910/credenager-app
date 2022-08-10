package com.credenager.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.credenager.R;
import com.credenager.SettingsActivity;
import com.credenager.adapters.CredAdapter;
import com.credenager.adapters.GroupAdapter;
import com.credenager.dialogs.CredentialDialog;
import com.credenager.dialogs.GroupDialog;
import com.credenager.utils.Data;
import com.credenager.utils.Globals;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomePageFragment extends Fragment {
    private View bgOverlay;
    private LinearLayout addGroupLayout;
    private LinearLayout addCredLayout;
    private FloatingActionButton addFab;
    private RecyclerView groupRecyclerview, credRecyclerview;
    private TextView emptyView;
    public RecyclerView.Adapter groupAdapter, credAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout searchPageLink = view.findViewById(R.id.home_search_link);
        ImageButton searchButton = view.findViewById(R.id.home_search_button);
        ImageButton settingsButton = view.findViewById(R.id.home_settings_button);
        bgOverlay = view.findViewById(R.id.home_bg_overlay);
        addGroupLayout = view.findViewById(R.id.home_add_group_layout);
        FloatingActionButton addGroupFab = view.findViewById(R.id.home_add_group_fab);
        addCredLayout = view.findViewById(R.id.home_add_cred_layout);
        FloatingActionButton addCredFab = view.findViewById(R.id.home_add_cred_fab);
        addFab = view.findViewById(R.id.home_add_fab);
        groupRecyclerview = view.findViewById(R.id.group_recycler_view);
        credRecyclerview = view.findViewById(R.id.cred_recycler_view);
        emptyView = view.findViewById(R.id.empty_view_text);

        searchPageLink.setOnClickListener(this::goToSearchPage);
        searchButton.setOnClickListener(this::goToSearchPage);
        addFab.setOnClickListener(this::addItemFabClick);
        bgOverlay.setOnClickListener(this::addItemFabClick);
        addCredFab.setOnClickListener(this::showAddCredDialog);
        addGroupFab.setOnClickListener(this::showAddGroupDialog);
        settingsButton.setOnClickListener(this::goToSettingsPage);

        groupRecyclerview.setLayoutManager(new GridLayoutManager(getContext(), 2));
        groupAdapter = new GroupAdapter(getContext());
        groupRecyclerview.setAdapter(groupAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        credRecyclerview.setLayoutManager(manager);
        credAdapter = new CredAdapter(getContext(), null, false, this::checkEmptyView);
        credRecyclerview.setAdapter(credAdapter);
        checkEmptyView();

    }

    public void checkEmptyView() {
        if (credAdapter.getItemCount() == 0 && groupAdapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            credRecyclerview.setVisibility(View.GONE);
            groupRecyclerview.setVisibility(View.GONE);
        }
        else {
            emptyView.setVisibility(View.GONE);
            credRecyclerview.setVisibility(View.VISIBLE);
            groupRecyclerview.setVisibility(View.VISIBLE);
        }
    }

    private void showAddGroupDialog(View view) {
        addItemFabClick(view);
        GroupDialog dialog = new GroupDialog(requireContext(), result -> {
            if (result) {
                groupAdapter.notifyItemInserted(Data.groups.size()-1);
                checkEmptyView();
            }
        });
        dialog.show();
    }

    private void showAddCredDialog(View view) {
        addItemFabClick(view);
        new CredentialDialog(requireContext(), null, result -> {
            if (result){
                if (Data.cachedCred.get(null) == null) {
                    Data.cachedCred.put(null, Data.getCredentialsByGroupId(null));
                }
                credAdapter.notifyItemInserted(Data.cachedCred.get(null).size() - 1);
                checkEmptyView();
            }
        }).show();
    }

    private void addItemFabClick(View view) {
        int[] credLayoutPosition = new int[2];
        addCredLayout.getLocationOnScreen(credLayoutPosition);
        int[] groupLayoutPosition = new int[2];
        addGroupLayout.getLocationOnScreen(groupLayoutPosition);
        int[] addFabPosition = new int[2];
        addFab.getLocationOnScreen(addFabPosition);

        if (addFab.getRotation() == 0) {
            addFab.animate().rotation(45f).start();
            bgOverlay.animate().alpha(1).withStartAction(() -> bgOverlay.setVisibility(View.VISIBLE)).start();
            addCredLayout.animate().translationY(0).alpha(1).withStartAction(() -> addCredLayout.setVisibility(View.VISIBLE)).start();
            addGroupLayout.animate().translationY(0).alpha(1).withStartAction(() -> addGroupLayout.setVisibility(View.VISIBLE)).start();
        }
        else {
            addFab.animate().rotation(0).start();
            bgOverlay.animate().alpha(0).withEndAction(() -> bgOverlay.setVisibility(View.INVISIBLE)).start();
            addCredLayout.animate().translationY((addFabPosition[1] - credLayoutPosition[1])).alpha(0).withEndAction(() -> addCredLayout.setVisibility(View.INVISIBLE)).start();
            addGroupLayout.animate().translationY(addFabPosition[1] - groupLayoutPosition[1]).alpha(0).withEndAction(() -> addGroupLayout.setVisibility(View.INVISIBLE)).start();
        }
    }

    private void goToSearchPage(View view) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_container, new SearchPageFragment(), Globals.SEARCH_FRAGMENT_TAG)
                .commit();
    }

    private void goToSettingsPage(View view) {
        requireActivity().startActivity(new Intent(getActivity(), SettingsActivity.class));
        requireActivity().finish();
        requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_from_left);
    }
}
