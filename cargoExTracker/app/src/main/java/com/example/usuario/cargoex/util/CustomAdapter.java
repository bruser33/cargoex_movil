package com.example.usuario.cargoex.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cargo.usuario.cargoex.R;

public class CustomAdapter extends BaseAdapter {
    Context context;

    String[] fruit;
    LayoutInflater inflter;

    public CustomAdapter(Context applicationContext, String[] fruit) {
        this.context = applicationContext;
        this.fruit = fruit;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return fruit.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.spinner_custom_layout, null);
        TextView names = (TextView) view.findViewById(R.id.textView);
        if(i%2==0){
            names.setBackgroundResource(R.drawable.mybordergray);
        }
        names.setText(fruit[i]);
        return view;
    }
}
