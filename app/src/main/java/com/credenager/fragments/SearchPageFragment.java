package com.credenager.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.credenager.models.Credential;
import com.credenager.R;
import com.credenager.adapters.CredSearchAdapter;
import com.credenager.utils.Data;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SearchPageFragment extends Fragment {
    private EditText searchEditText;
    private RecyclerView recyclerView;
    private CredSearchAdapter adapter;
    private TextView emptyTextView;
    private ScrollView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton goBackButton = view.findViewById(R.id.search_go_back_button);
        searchEditText = view.findViewById(R.id.search_search_edittext);
        emptyView = view.findViewById(R.id.search_empty_view);
        emptyTextView = view.findViewById(R.id.search_empty_view_text);
        recyclerView = view.findViewById(R.id.cred_search_recycler_view);

        focusEditText();
        goBackButton.setOnClickListener(this::gotoHomePage);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        addEditTextListener();
        updateResults("");
    }

    private void focusEditText(){
        searchEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void addEditTextListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            private Timer timer = new Timer();

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(final Editable editable) {
                timer.cancel();
                timer = new Timer();
                long DELAY = 100;
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        updateResults(editable.toString());
                    }
                }, DELAY);
            }
        });
    }

    private void updateResults(String keyword){
        List<Credential> credentials = Data.searchCredentials(keyword);

        new Handler(Looper.getMainLooper()).post(()-> {
            if (credentials.size() == 0){
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                if (keyword.isEmpty())
                    emptyTextView.setText("Search For Credentials!");
                else
                    emptyTextView.setText("No Matching Credential Found!");
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter = new CredSearchAdapter(getContext(), credentials);
                recyclerView.setAdapter(adapter);
            }
        });
    }

    private void gotoHomePage(View view) {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .replace(R.id.fragment_container, new HomePageFragment())
                .commit();
    }
}
