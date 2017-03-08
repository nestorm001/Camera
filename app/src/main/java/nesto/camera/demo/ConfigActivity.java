package nesto.camera.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created on 2017/3/7.
 * By nesto
 */

public class ConfigActivity extends BaseActivity {
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
    }

    public void start(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }
}
