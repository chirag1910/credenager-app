package com.credenager.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.credenager.R;
import com.credenager.adapters.CredAdapter;
import com.credenager.dialogs.ConfirmationDialog;
import com.credenager.dialogs.CredentialDialog;
import com.credenager.dialogs.GroupDialog;
import com.credenager.utils.Api;
import com.credenager.utils.Data;
import com.credenager.utils.Globals;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class GroupPageFragment extends Fragment {
    private final String groupId;
    private String credId = null;
    private String groupName;
    private boolean menuOpen = false;
    private CollapsingToolbarLayout appBar;
    private View bgOverlay, bgAppbarOverlay;
    private FloatingActionButton menuFab, addFab, editFab, deleteFab, addBarFab;
    private RecyclerView credRecyclerview;
    private NestedScrollView emptyView;
    public RecyclerView.Adapter credAdapter;

    public GroupPageFragment(String groupId) {
        this.groupId = groupId;
    }
    public GroupPageFragment(String groupId, String credId) {
        this.groupId = groupId;
        this.credId = credId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.group_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.group_toolbar);
        appBar = view.findViewById(R.id.group_appbar);
        AppBarLayout appBarLayout = view.findViewById(R.id.group_appbar_layout);
        bgOverlay = view.findViewById(R.id.group_bg_overlay);
        bgAppbarOverlay = view.findViewById(R.id.group_appbar_bg_overlay);
        addBarFab = view.findViewById(R.id.group_add_bar_fab);
        menuFab = view.findViewById(R.id.group_menu_fab);
        addFab = view.findViewById(R.id.group_add_cred_fab);
        editFab = view.findViewById(R.id.group_edit_group_fab);
        deleteFab = view.findViewById(R.id.group_delete_group_fab);
        credRecyclerview = view.findViewById(R.id.group_cred_recycler_view);
        emptyView = view.findViewById(R.id.empty_view);

        setGroupName();
        requireActivity().setActionBar(toolbar);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().getActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left);

        bgOverlay.setOnClickListener(this::menuFabClick);
        bgAppbarOverlay.setOnClickListener(this::menuFabClick);
        menuFab.setOnClickListener(this::menuFabClick);
        addFab.setOnClickListener(this::addFabOnClick);
        addBarFab.setOnClickListener(this::addFabOnClick);
        editFab.setOnClickListener(this::editFabOnClick);
        deleteFab.setOnClickListener(this::deleteFabOnClick);
        appBarLayout.addOnOffsetChangedListener(this::onAppbarScroll);

        credRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        credAdapter = new CredAdapter(getContext(), groupId, true, this::checkEmptyView);
        credRecyclerview.setAdapter(credAdapter);

        checkEmptyView();
        checkScroll();
    }
    public void checkEmptyView() {
        if (credAdapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            credRecyclerview.setVisibility(View.GONE);
        }
        else {
            emptyView.setVisibility(View.GONE);
            credRecyclerview.setVisibility(View.VISIBLE);
        }
    }

    private void onAppbarScroll(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset == 0)
            addBarFab.show();
        else
            addBarFab.hide();
    }

    private void deleteFabOnClick(View view) {
        menuOpen = true;
        menuFabClick(view);
        new ConfirmationDialog(
                requireContext(),
                "All group credentials will also be deleted!",
                "Delete Anyway",
                result ->
                {
                    if (result){
                        enableButtons(false);

                        Api.deleteGroup(Globals.JWT_TOKEN, groupId, response -> {
                            try {
                                if (((Integer) response.get("code")) == 200) {
                                    Data.deleteGroup(groupId);
                                    new Handler(Looper.getMainLooper()).post(this::gotoHomePage);
                                } else {
                                    String error = response.getString("error");
                                    new Handler(Looper.getMainLooper()).post(() ->
                                            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show()
                                    );
                                }
                            } catch (Exception e) {
                                new Handler(Looper.getMainLooper()).post(() ->
                                        Toast.makeText(getContext(), "Unknown Error Occurred!", Toast.LENGTH_LONG).show()
                                );
                            }

                            new Handler(Looper.getMainLooper()).post(() -> enableButtons(true));
                        });
                    }
                }
        ).show();
    }

    private void editFabOnClick(View view) {
        menuOpen = true;
        menuFabClick(view);
        new GroupDialog(requireContext(), groupId, groupName, result -> {
            if (result)
                setGroupName();
        }).show();
    }

    private void addFabOnClick(View view) {
        menuOpen = true;
        menuFabClick(view);
        new CredentialDialog(requireContext(), groupId, result -> {
            if (result) {
                if (Data.cachedCred.get(groupId) == null) {
                    Data.cachedCred.put(groupId, Data.getCredentialsByGroupId(groupId));
                }
                credAdapter.notifyItemRangeChanged(Data.cachedCred.get(groupId).size() - 2, 2);
                checkEmptyView();
            }
        }).show();
    }

    public void setGroupName() {
        try{
            if (groupId == null)
                appBar.setTitle("Ungrouped");
            else{
                groupName = Data.getGroupById(groupId).getName();
                appBar.setTitle(groupName);
            }
        } catch (Exception ignored){

        }
    }

    private void menuFabClick(View view) {
        int[] addFabPosition = new int[2];
        addFab.getLocationOnScreen(addFabPosition);
        int[] editFabPosition = new int[2];
        editFab.getLocationOnScreen(editFabPosition);
        int[] deleteFabPosition = new int[2];
        deleteFab.getLocationOnScreen(deleteFabPosition);
        int[] menuFabPosition = new int[2];
        menuFab.getLocationOnScreen(menuFabPosition);

        if (!menuOpen) {
            menuFab.setImageDrawable(getContext().getDrawable(R.drawable.ic_close));
            bgOverlay.animate().alpha(1).withStartAction(() -> bgOverlay.setVisibility(View.VISIBLE)).start();
            bgAppbarOverlay.animate().alpha(1).withStartAction(() -> bgAppbarOverlay.setVisibility(View.VISIBLE)).start();
            addFab.animate().translationY(0).alpha(1).withStartAction(() -> addFab.setVisibility(View.VISIBLE)).start();
            editFab.animate().translationY(0).alpha(1).withStartAction(() -> editFab.setVisibility(View.VISIBLE)).start();
            deleteFab.animate().translationY(0).alpha(1).withStartAction(() -> deleteFab.setVisibility(View.VISIBLE)).start();
        }
        else {
            menuFab.setImageDrawable(getContext().getDrawable(R.drawable.ic_menu));
            bgOverlay.animate().alpha(0).withStartAction(() -> bgOverlay.setVisibility(View.INVISIBLE)).start();
            bgAppbarOverlay.animate().alpha(0).withStartAction(() -> bgAppbarOverlay.setVisibility(View.INVISIBLE)).start();
            addFab.animate().translationY((menuFabPosition[1] - addFabPosition[1])).alpha(0).withEndAction(() -> addFab.setVisibility(View.INVISIBLE)).start();
            editFab.animate().translationY((menuFabPosition[1] - editFabPosition[1])).alpha(0).withEndAction(() -> editFab.setVisibility(View.INVISIBLE)).start();
            deleteFab.animate().translationY((menuFabPosition[1] - deleteFabPosition[1])).alpha(0).withEndAction(() -> deleteFab.setVisibility(View.INVISIBLE)).start();
        }

        menuOpen = !menuOpen;
    }

    private void enableButtons(boolean enable) {
        addBarFab.setEnabled(enable);
        addFab.setEnabled(enable);
        editFab.setEnabled(enable);
        deleteFab.setEnabled(enable);

        if (!enable) {
            addBarFab.setAlpha(0.3f);
            addFab.setAlpha(0.3f);
            editFab.setAlpha(0.3f);
            deleteFab.setAlpha(0.3f);
        }
        else {
            addBarFab.setAlpha(1f);
            addFab.setAlpha(1f);
            editFab.setAlpha(1f);
            deleteFab.setAlpha(1f);
        }
    }

    private void checkScroll() {
        if (credId != null){
            int pos = Data.getCredentialPosition(groupId, credId);
            credRecyclerview.scrollToPosition(pos);
        }
    }

    private void gotoHomePage(){
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_from_right)
                .replace(R.id.fragment_container, new HomePageFragment())
                .commit();
    }
}
