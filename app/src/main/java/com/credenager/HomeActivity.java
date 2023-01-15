package com.credenager;

import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import com.credenager.dialogs.ConfirmationDialog;
import com.credenager.fragments.HomePageFragment;
import com.credenager.fragments.HomeSkeletonFragment;
import com.credenager.fragments.SearchPageFragment;
import com.credenager.utils.Api;
import com.credenager.utils.Data;
import com.credenager.utils.Globals;
import com.credenager.utils.Session;

import java.util.HashMap;

public class HomeActivity extends FragmentActivity {
    private ImageView bg;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        setRequestedOrientation(getResources().getConfiguration().orientation);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeSkeletonFragment()).commit();
        if (Data.dataString == null)
            getData();
        else
            gotoHomeFragment();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            bg = findViewById(R.id.app_bg);
            setBg();
        }
    }

    private void setBg() {
        final Matrix matrix = bg.getImageMatrix();

        float scale;
        final int viewWidth = bg.getWidth();
        final int viewHeight = bg.getHeight();
        final int drawableWidth = getDrawable(R.drawable.ic_bg).getIntrinsicWidth();
        final int drawableHeight = getDrawable(R.drawable.ic_bg).getIntrinsicHeight();

        if (drawableWidth * viewHeight > drawableHeight * viewWidth) {
            scale = (float) viewHeight / (float) drawableHeight;
        } else {
            scale = (float) viewWidth / (float) drawableWidth;
        }

        matrix.setScale(scale, scale);
        bg.setImageMatrix(matrix);
    }

    private void getData(){
        Api.getUser(Session.JWT_TOKEN, response -> {
            try{
                int responseCode = response.getInt("code");
                if (responseCode == 200) {
                    try{
                        final String data = response.getString("data");
                        Data.set(data);

                        Boolean allowOffline = (Boolean) Globals.getSettings(this).getOrDefault(Globals.OFFLINE_KEY, false);
                        if (Boolean.TRUE.equals(allowOffline)) {
                            Globals.saveData(this, Data.dataString);
                        }
                        else {
                            Globals.saveData(this, null);
                        }
                        new Handler(Looper.getMainLooper()).post(this::gotoHomeFragment);
                    }catch (Exception e){
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(this, "Error Parsing Data", Toast.LENGTH_LONG).show()
                        );
                    }
                }
                else if (responseCode == 502) {
                    String storedData = Globals.getData(this);

                    if (Session.APP_OFFLINE_MODE) {
                        if (storedData != null){
                            try{
                                Data.set(storedData);
                                new Handler(Looper.getMainLooper()).post(this::gotoHomeFragment);
                            } catch (Exception e){
                                new Handler(Looper.getMainLooper()).post(() ->
                                        Toast.makeText(this, "Error Parsing Data", Toast.LENGTH_LONG).show()
                                );
                            }
                        }
                    }
                    else{
                        String error = response.getString("error");
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                        );
                    }
                }
                else{
                    String error = response.getString("error");
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                    );
                }
            } catch (Exception e){
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(this, "Unknown Error Occurred!", Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void gotoHomeFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, new HomePageFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        Fragment groupFragment = getSupportFragmentManager().findFragmentByTag(Globals.GROUP_FRAGMENT_TAG);
        Fragment searchFragment = getSupportFragmentManager().findFragmentByTag(Globals.SEARCH_FRAGMENT_TAG);
        Fragment searchResultRedirect = getSupportFragmentManager().findFragmentByTag(Globals.FROM_SEARCH_FRAGMENT_TAG);

        if (groupFragment != null && groupFragment.isVisible()){
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_from_right)
                    .replace(R.id.fragment_container, new HomePageFragment())
                    .commit();
        } else if (searchFragment != null && searchFragment.isVisible()){
            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .replace(R.id.fragment_container, new HomePageFragment())
                    .commit();
        } else if (searchResultRedirect != null && searchResultRedirect.isVisible()){
            if (searchFragment != null){
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_from_right)
                        .remove(searchResultRedirect)
                        .show(searchFragment)
                        .commit();
            } else{
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_from_right)
                        .replace(R.id.fragment_container, new SearchPageFragment())
                        .commit();
            }
        } else{
            new ConfirmationDialog(this, "Exit?", "Sure", (result) -> {
                if (result)
                    this.finish();
            }).show();
        }
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }
}
