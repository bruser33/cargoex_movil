package com.example.usuario.cargoex.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.cargo.usuario.cargoex.R;

public class SqliteHelper extends SQLiteOpenHelper{

    final String createTable = "CREATE TABLE acciones (id TEXT, fechaIngreso TEXT, latitud TEXT, longitud TEXT, accion TEXT, fechaEnvio TEXT)";
    public SqliteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int lastVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS acciones");
        onCreate(db);
    }

}
