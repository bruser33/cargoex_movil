package com.example.usuario.cargoex.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.cargo.usuario.cargoex.R;

public class Modal2 extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mymodal2);
        TextView err= (TextView) findViewById(R.id.error);


        String mensaje = getIntent().getStringExtra("error");
        err.setText(mensaje);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.85),(int)(height*.55));
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x =0;
        params.y = -20;
        getWindow().setAttributes(params);

    }
    public void si(View v){
        Intent output = new Intent();
        output.putExtra("status", "true");
        setResult(RESULT_OK, output);
        this.finish();
    }
    public void no(View v){
        Intent output = new Intent();
        output.putExtra("status", "false");
        setResult(RESULT_OK, output);
        this.finish();

    }


}
