package com.credenager;

import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.credenager.fragments.SettingsPageFragment;

public class SettingsActivity extends FragmentActivity {
    private ImageView bg;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsPageFragment()).commit();
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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_from_right);
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }
}
