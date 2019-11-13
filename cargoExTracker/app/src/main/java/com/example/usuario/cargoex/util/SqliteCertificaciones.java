package com.example.usuario.cargoex.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cargo.usuario.cargoex.R;

public class SqliteCertificaciones extends SQLiteOpenHelper{

    final String createTable = "CREATE TABLE certificaciones (id TEXT, codChofer TEXT, rut TEXT, nombreReceptor TEXT, latitud TEXT, longitud TEXT, od TEXT, tn TEXT, nota TEXT, foto1 TEXT, foto2 TEXT, foto3 TEXT, fechaIngreso TEXT, fechaEnvio TEXT, status TEXT,codEstado TEXT, tipoCertificacion TEXT, multientrega TEXT, decCode TEXT, idTelefono TEXT, coneccion TEXT, canal TEXT , idCliente TEXT , idSucursal TEXT , bultos TEXT , telefono TEXT , mail TEXT, dia TEXT )";
    final String createTableAcciones = "CREATE TABLE acciones (id TEXT, fechaIngreso TEXT, latitud TEXT, longitud TEXT, accion TEXT, fechaEnvio TEXT)";

    public SqliteCertificaciones(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        Log.e("db","llego a creacion de bd");

    }

    @Override
    public void onCreate(SQLiteDatabase db){
        Log.e("certificaciones","certificaciones creadas");
        db.execSQL(createTable);
        db.execSQL(createTableAcciones);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int lastVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS certificaciones");
        db.execSQL("DROP TABLE IF EXISTS acciones");
        onCreate(db);
    }

}
