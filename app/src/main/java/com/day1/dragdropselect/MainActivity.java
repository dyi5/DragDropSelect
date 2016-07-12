package com.day1.dragdropselect;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        attachObserver();
    }

    public void attachObserver() {
        DragDropSelect c = (DragDropSelect)findViewById(R.id.customSlider);
        c.registerObserver(new DragDropSelect.TheObserver() {
            public void callback(int i) {
                nodeSelected(i);
            }
        });
    }

    private void nodeSelected(int i) {
        // method to invoke for selecting node i
    }
}
