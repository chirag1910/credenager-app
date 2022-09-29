package com.credenager;

import android.graphics.Matrix;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.credenager.fragments.KeyPageFragment;
import com.credenager.fragments.LoginPageFragment;
import com.credenager.fragments.SplashPageFragment;
import com.credenager.fragments.WelcomePageFragment;
import com.credenager.utils.Globals;

public class MainActivity extends FragmentActivity {
    private ImageView bg;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        setRequestedOrientation(getResources().getConfiguration().orientation);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SplashPageFragment()).commit();
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
        Fragment signupFragment = getSupportFragmentManager().findFragmentByTag(Globals.SIGNUP_FRAGMENT_TAG);
        Fragment loginFragment = getSupportFragmentManager().findFragmentByTag(Globals.LOGIN_FRAGMENT_TAG);
        Fragment resetPassFragment = getSupportFragmentManager().findFragmentByTag(Globals.RESET_PASS_FRAGMENT_TAG);
        Fragment resetKeyFragment = getSupportFragmentManager().findFragmentByTag(Globals.RESET_KEY_FRAGMENT_TAG);

        if ((loginFragment != null && loginFragment.isVisible()) || (signupFragment != null && signupFragment.isVisible())) {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_from_right).replace(R.id.fragment_container, new WelcomePageFragment(-1,-1)).commit();
        } else if (resetPassFragment != null && resetPassFragment.isVisible()) {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_from_right).replace(R.id.fragment_container, new LoginPageFragment(), Globals.LOGIN_FRAGMENT_TAG).commit();
        }else if (resetKeyFragment != null && resetKeyFragment.isVisible()) {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_from_right).replace(R.id.fragment_container, new KeyPageFragment(-1, -1)).commit();
        } else {
            this.finish();
        }
    }
}
