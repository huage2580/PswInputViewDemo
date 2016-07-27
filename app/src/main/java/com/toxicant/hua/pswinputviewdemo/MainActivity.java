package com.toxicant.hua.pswinputviewdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.toxicant.hua.pswinputview.PswInputView;

public class MainActivity extends AppCompatActivity {
    PswInputView view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view= (PswInputView) findViewById(R.id.psw_input);
        view.setInputCallBack(new PswInputView.InputCallBack() {
            @Override
            public void onInputFinish(String result) {
                Toast.makeText(MainActivity.this,result,Toast.LENGTH_SHORT).show();
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.clearResult();
                    }
                },2000);
            }
        });
    }
}
