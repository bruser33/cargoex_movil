package com.example.usuario.cargoex;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cargo.usuario.cargoex.R;
import com.example.usuario.cargoex.util.AsyncResponse;
import com.example.usuario.cargoex.util.Modal;
import com.example.usuario.cargoex.util.ModalGestiones;
import com.example.usuario.cargoex.util.ModalVersion;
import com.example.usuario.cargoex.util.SqliteCertificaciones;
import com.example.usuario.cargoex.util.SqliteHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Dashboard extends AppCompatActivity implements AsyncResponse {
    private FusedLocationProviderClient client;
    String latitud, longitud = "";
    int devoluciones, faltaTransmitir, retirados;
    int entregas;
    SharedPreferences prefs ;
    SqliteCertificaciones conn;
    TextView transmisiones, version, nombre;
    Button devolucion, entregados, retiros,total;
    Intent modal,modalGestiones;
    String codigo, od,fechaGestion,numerood,base,path;
    int sincro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        nombre = (TextView) findViewById(R.id.nombre);
        modal = new Intent(this, Modal.class);
        modalGestiones = new Intent(this, ModalGestiones.class);
        conn = new SqliteCertificaciones(this, "bd_certificaciones", null, R.string.versionDB);
        prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
        String name = prefs.getString("nombre", "");
        codigo = prefs.getString("codigo", "");
        if(codigo.equals("0")){
            this.finish();
        }
        nombre.setText(name);
        //capturo fecha actual y la pongo en el dashboard
        sincro = 0;
        TextView fecha = (TextView) findViewById(R.id.fecha);
        devolucion = (Button) findViewById(R.id.devoluciones);
        entregados = (Button) findViewById(R.id.entregados);
        retiros = (Button) findViewById(R.id.retiros);
        total = (Button) findViewById(R.id.totalGestiones);
        transmisiones = (TextView) findViewById(R.id.transmisiones);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy ", Locale.getDefault());
        Date date = new Date();
        String fechaHoy = dateFormat.format(date);
        fecha.setText(fechaHoy);
        //registro en base de datos local la accion
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        Date date2 = new Date();
        String fecha2 = dateFormat2.format(date2);
        requestPermission();
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
            String version_name = packageInfo.versionName;
            Log.e("acciones","antes de preguntar la posicion");

            Log.e("versionAMIENTOES es ",version_name);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(Dashboard.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ;
        }
        client.getLastLocation().addOnSuccessListener(Dashboard.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //tomo latitud y longitud
                    latitud = location.getLatitude() + "";
                    longitud = location.getLongitude() + "";

                } /* else {
                    modal.putExtra("error", "Activa el gps, esta desactivado o presenta fallas");
                    startActivity(modal);
                }*/

            }
        });

    //    Log.e("gps",this.getLocationMode()+"");

        eliminarRegistrosViejos();
        consultarCertificaciones();
        path= prefs.getString("path", "");
        numerood=prefs.getString("numerood", "");
        fechaGestion=prefs.getString("fechagestion","");
        Log.e("fotohistorialc","path es"+path);
        if (!path.equals("")) {
            Log.e("fotohistorialc","sisas entro");
            comprimir(path);
            SQLiteDatabase db = conn.getWritableDatabase();
            String[] parametros = {numerood, fechaGestion};
            ContentValues values = new ContentValues();
            values.put("foto1", base);
            db.update("certificaciones", values, "od = ? and fechaIngreso = ? ", parametros);
            db.close();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("numerood", "");
            editor.putString("fechagestion", "");
            editor.putString("path", "");
            editor.commit();
        }
        fotosLibres();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Log.e("fotohistorialc","entro aqui en resutl y se veran datos ");

                path=data.getStringExtra("path");
                numerood=data.getStringExtra("numero");
                fechaGestion=data.getStringExtra("fecha");

                Log.e("fotohistorialc","path en dash es"+path);
                Log.e("fotohistorialc","numero od  en dash es"+numerood);
                Log.e("fotohistorialc","fecha gestion en dash es"+fechaGestion);


                if (!path.equals("")) {
                    Log.e("fotohistorialc","sisas entro");
                    comprimir(path);

                    SQLiteDatabase db = conn.getWritableDatabase();
                    String[] parametros = {numerood, fechaGestion};
                    ContentValues values = new ContentValues();
                    Log.e("fotohistorialc",base);
                    values.put("foto1", base);
                    int a = db.update("certificaciones", values, "od = ? and fechaIngreso = ? ", parametros);
                    db.close();
                    Log.e("fotohistorialc","actualizo bd con id "+a);
                    sincronizarFoto();
                }
               // sincronizarFoto();
            }
        }
    }
    public int getLocationMode()
    {
        try {
            return Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void gestionesEntregadas(View v){
        modalGestiones.putExtra("vista", "entregas");
        modalGestiones.putExtra("codigo", codigo);
        startActivityForResult(modalGestiones,1);
    }
    public void gestionesNoEntregadas(View v){
        modalGestiones.putExtra("vista", "noentregas");
        modalGestiones.putExtra("codigo", codigo);
        startActivityForResult(modalGestiones,1);
    }
    public void gestionesRetiradas(View v){
        modalGestiones.putExtra("vista", "retiros");
        modalGestiones.putExtra("codigo", codigo);
        startActivityForResult(modalGestiones,1);
    }
    public void eliminarRegistrosViejos() {
        Log.e("fecha", "llego a eliminar");
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date2 = new Date();
        String fecha2 = dateFormat2.format(date2);
        Date myDate = null;
        try {
            myDate = dateFormat2.parse(fecha2);
        Date newDate = new Date(myDate.getTime() - 259200000L);
        String date = dateFormat2.format(newDate);
        SQLiteDatabase db = conn.getWritableDatabase();
        String[] parametros = {"null", date};
        String[] parametros2 = {"null"};
        db.delete("certificaciones", "status != ? and dia <= ?", parametros);
        db.delete("acciones", "fechaEnvio != ? ", parametros2);
        db.delete("acciones", "latitud == ? ", parametros2);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }
// aqui hay un bug en
    @Override
    public void onResume() {
        super.onResume();
        if(codigo.equals("0")){
            this.finish();
        }
        faltaTransmitir = 0;
        devoluciones = 0;
        entregas = 0;
        retirados = 0;
        consultarCertificaciones();
        Log.e("state", "resumido dashboard");
        //registro en base de datos local la accion
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        Date date2 = new Date();
        String fecha2 = dateFormat2.format(date2);
        /*
        Log.e("acciones","Registro Acciones");
        if (ActivityCompat.checkSelfPermission(Dashboard.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) { }
        client.getLastLocation().addOnSuccessListener(Dashboard.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    SQLiteDatabase db = conn.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    Date date2 = new Date();
                    String fecha2 = dateFormat2.format(date2);
                    if(location!=null){
                        latitud = location.getLatitude() + "";
                        longitud = location.getLongitude() + "";
                    }
                    if (latitud == null || longitud == null || latitud.equals("") || longitud.equals("") || latitud.equals(" ") || longitud.equals(" ")) {
                        if(ultimaPosicionAccion()){
                            values.put("id", codigo);
                            values.put("fechaIngreso", fecha2);
                            values.put("latitud", latitud);
                            values.put("longitud", longitud);
                            values.put("accion", "dashboard");
                            values.put("fechaEnvio", "null");
                            Long id_result = db.insert("acciones", codigo, values);
                            db.close();
                        }else if(ultimaPosicionGestion()){

                            values.put("id", codigo);
                            values.put("fechaIngreso", fecha2);
                            values.put("latitud", latitud);
                            values.put("longitud", longitud);
                            values.put("accion", "dashboard");
                            values.put("fechaEnvio", "null");
                            Long id_result = db.insert("acciones", codigo, values);
                            db.close();
                        }

                    } else {
                        values.put("id", codigo);
                        values.put("fechaIngreso", fecha2);
                        values.put("latitud", latitud);
                        values.put("longitud", longitud);
                        values.put("accion", "dashboard");
                        values.put("fechaEnvio", "null");
                        Long id_result = db.insert("acciones", codigo, values);
                        db.close();

                    }
                }
            });

            */
        if (isNetDisponible()) {
            sincronizar();
        }
    }
    public boolean fotosLibres(){
       SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        String fechaHoy = dateFormat.format(date);
        String diahoy[]=fechaHoy.split(" ");

        SQLiteDatabase db = conn.getReadableDatabase();
        try {
            final Cursor cursor = db.rawQuery("SELECT * FROM certificaciones WHERE codChofer = " + codigo, null);
            Log.e("tabla","el tamaño total es "+cursor.getCount());
            while (cursor.moveToNext()) {
           /*     Log.e("fotoslibres", "hoy es"+hoy.getTime() );
                Log.e("fotoslibres", "hoy en milisegundos"+hoy.getTimeInMillis());
*/

                Log.e("fotoslibres", "El identificacdor es " + cursor.getString(12));
                Log.e("fotoslibres", "El la hora de hoy es  " + diahoy[1]);

                String fecha[] = cursor.getString(12).split(" ");
           //     String aux1[]=fecha[0].split("/");

                java.text.DateFormat df = new java.text.SimpleDateFormat("hh:mm:ss");

                java.util.Date date2 = df.parse(diahoy[1]);
                java.util.Date date1 = df.parse(fecha[1]);
                long diff = date2.getTime() - date1.getTime();


                long timeInSeconds = diff / 1000;
                long hours, minutes, seconds;
                hours = timeInSeconds / 3600;
                timeInSeconds = timeInSeconds - (hours * 3600);
                minutes = timeInSeconds / 60;


                Log.e("fotoslibres", "dia a restar " + fecha[0]);
                Log.e("fotoslibres", "El la hora a restar " + fecha[1]);
                Log.e("fotoslibres", "dif  " + diff );
                Log.e("fotoslibres", "dif en horas  " + hours);

                Log.e("fotoslibres", "dif en minutos  " + minutes);

                if (!cursor.getString(27).equals(diahoy[0]) && cursor.getString(14).equals("null") && ( cursor.getString(9).equals("FALSE") &&  cursor.getString(10).equals("FALSE") &&  cursor.getString(11).equals("FALSE")) &&  cursor.getString(17).equals("TRUE")  &&  !cursor.getString(16).equals("devolucion")) {
                    return false;
                }
                if((hours>0 || minutes>20) && cursor.getString(14).equals("null") && ( cursor.getString(9).equals("FALSE") &&  cursor.getString(10).equals("FALSE") &&  cursor.getString(11).equals("FALSE")) &&  cursor.getString(17).equals("TRUE")  &&  (cursor.getString(16).equals("normal")|| cursor.getString(16).equals("biometrica") )){
                   return false;
                }
                if( hours>5 && cursor.getString(14).equals("null") && ( cursor.getString(9).equals("FALSE") &&  cursor.getString(10).equals("FALSE") &&  cursor.getString(11).equals("FALSE")) &&  cursor.getString(17).equals("TRUE")  &&  cursor.getString(16).equals("retiro") ){
                    return false;
                }
                }
            } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
            db.close();
        }
        return true;
    }

    public boolean ultimaPosicionAccion(){
        String codigo = prefs.getString("codigo", "");
        Log.e("ULTIMA ACCION","llego al boton ");
        SQLiteDatabase db = conn.getReadableDatabase();
        Log.e("ULTIMA ACCION", "Va a consultar acciones");
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM acciones WHERE id = " + codigo, null);
            Log.e("ULTIMA ACCION", "tamaño de las acciones es " + cursor.getCount());
            if(cursor == null && !cursor.isClosed()){
                cursor.close();
                db.close();
                return false;
            }
            cursor.moveToLast();
            if(cursor.getString(2).equals("")|| cursor.getString(2).equals(null)||cursor.getString(2).equals("null")){
                cursor.close();
                db.close();
                return false;
            }else{
                Log.e("ULTIMA ACCION",cursor.getString(0)+" ---"+cursor.getString(1)+"---"+cursor.getString(2)+"---"+cursor.getString(3)+"---"+cursor.getString(4)+"---"+cursor.getString(5));
                latitud=cursor.getString(2);
                longitud=cursor.getString(3);
                cursor.close();
                db.close();
                return true;
            }
        } catch (Exception e) {
            Log.e("ULTIMA ACCION","no encontro al consultar");
            db.close();
            return false;
        }
    }
    public boolean ultimaPosicionGestion(){
        String codigo = prefs.getString("codigo", "");
        //  Log.e("ULTIMA ACCION","llego al boton ");
        SQLiteDatabase db = conn.getReadableDatabase();
        //   Log.e("ULTIMA ACCION", "Va a consultar acciones");
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM certificaciones WHERE codChofer = " + codigo, null);
            //     Log.e("ULTIMA ACCION", "tamaño de las acciones es " + cursor.getCount());
            if(cursor == null && !cursor.isClosed()){
                cursor.close();
                db.close();
                return false;
            }
            cursor.moveToLast();
            if(cursor.getString(4).equals("")|| cursor.getString(4).equals(null)||cursor.getString(4).equals("null")){
                cursor.close();
                db.close();
                return false;
            }else{
                Log.e("ULTIMA ACCION",cursor.getString(0)+" ---"+cursor.getString(1)+"---"+cursor.getString(2)+"---"+cursor.getString(3)+"---"+cursor.getString(4)+"---"+cursor.getString(5));
                latitud=cursor.getString(4);
                longitud=cursor.getString(5);
                cursor.close();
                db.close();
                return true;
            }
        } catch (Exception e) {
            Log.e("ULTIMA ACCION","no encontro al consultar");
            db.close();
            return false;
        }
    }
    private boolean isNetDisponible() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }
    public boolean isOnlineNet() {
        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");
            int val = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public void processFinish(String output,String identificadorServicio) {
        if (output.equals("false")) {
            Log.e("sate", "no hay internet");
        } else {
            if(identificadorServicio.equals("1")){
            //Actualizo el registro a ya mandado
            try {
                JSONObject jsonObject = new JSONObject(output);
                Log.e("status", "llego del servicio " + output);
                if (jsonObject.getString("success").compareTo("true") == 0 && !jsonObject.getString("ID_INSERCION").equals("0")) {
                    Log.e("sincro", "funciono sincronizacion");
                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    Date date2 = new Date();
                    String fecha2 = dateFormat2.format(date2);
                    SQLiteDatabase db = conn.getWritableDatabase();
                    String[] parametros = {jsonObject.getString("OD"), jsonObject.getString("FH_GESTION")};
                    String insercion = jsonObject.getString("ID_INSERCION");
                    ContentValues values = new ContentValues();
                    values.put("fechaEnvio", fecha2);
               //     values.put("status", insercion);
                    db.update("certificaciones", values, "od = ? and fechaIngreso = ? ", parametros);
                    db.close();
                    if (faltaTransmitir > 0) {
                        faltaTransmitir -= 1;
                        if (faltaTransmitir == 0) {
                            transmisiones.setText("ESTADO: SINCRONIZADO");
                        } else {
                            transmisiones.setText("(" + faltaTransmitir + ") NO SINCRONIZADAS");
                        }
                        sincronizar();
                    } else {
                        if (faltaTransmitir == 0) {
                            transmisiones.setText("ESTADO: SINCRONIZADO");
                        } else {
                            transmisiones.setText("(" + faltaTransmitir + ") NO SINCRONIZADAS");
                        }
                       return;
                    }


                } else {
                    modal.putExtra("error", "Fallo sincronizacion");
                    startActivity(modal);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            }else if(identificadorServicio.equals("2")){
                try {
                    JSONObject jsonObject = new JSONObject(output);
                    Log.e("status", "llego del servicio de acciones" + output);
                    if (jsonObject.getString("success").compareTo("true") == 0 ) {
                        Log.e("sincro", "funciono sincronizacion de acciones");

                        SQLiteDatabase db = conn.getWritableDatabase();
                        String[] parametros = { jsonObject.getString("FECHA_INGRESO")};
                        String insercion = jsonObject.getString("ID_INSERCION");
                        ContentValues values = new ContentValues();
                        values.put("fechaEnvio", insercion);
                        db.update("acciones", values, "fechaIngreso = ? ", parametros);
                        db.close();
                        sincronizarAcciones();


                    } else {
                        modal.putExtra("error", "Fallo sincronizacion");
                        startActivity(modal);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(identificadorServicio.equals("3")){
                //Actualizo el registro a ya mandado
                try {
                    JSONObject jsonObject = new JSONObject(output);
                    Log.e("status", "llego del servicio " + output);
                    if (jsonObject.getString("success").compareTo("true") == 0 && !jsonObject.getString("ID_INSERCION").equals("0")) {
                        Log.e("sincro", "funciono sincronizacion");
                        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                        Date date2 = new Date();
                        String fecha2 = dateFormat2.format(date2);
                        SQLiteDatabase db = conn.getWritableDatabase();
                        String[] parametros = {jsonObject.getString("OD"), jsonObject.getString("FH_GESTION")};
                        String insercion = jsonObject.getString("ID_INSERCION");
                        ContentValues values = new ContentValues();
                        values.put("status", insercion);
                        db.update("certificaciones", values, "od = ? and fechaIngreso = ? ", parametros);
                        db.close();

                        sincronizarFoto();


                    } else {
                        modal.putExtra("error", "Fallo sincronizacion");
                        startActivity(modal);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void goDevoluciones(View v) {
        if(fotosLibres()){
            Intent intent = new Intent(this, IngresoOd.class);
            intent.putExtra("vista", "devoluciones");
            startActivity(intent);
        }else{
            modal.putExtra("error", "Faltan fotos en ods, dirijase al historial y tome fotos alas ods marcadas en rojo (inicie por la ultima od en rojo)");
            startActivity(modal);
        }

    }

    public void goEntregas(View v) {
        if(fotosLibres()){
            Intent intent = new Intent(this, IngresoOd.class);
            intent.putExtra("vista", "entregas");
            startActivity(intent);
        }else{
            modal.putExtra("error", "Faltan fotos en ods, dirijase al historial y tome fotos alas ods marcadas en rojo (inicie por la ultima od en rojo)");
            startActivity(modal);
        }

    }

    public void goRetiros(View v) {
        if(fotosLibres()){
        Intent intent = new Intent(this, IngresoOd.class);
        intent.putExtra("vista", "retiros");
        startActivity(intent);
        }else{
        modal.putExtra("error", "Faltan fotos en ods, dirijase al historial y tome fotos alas ods marcadas en rojo (inicie por la ultima od en rojo)");
        startActivity(modal);
        }
    }

    public void goRetornos(View v) {
        if(fotosLibres()) {
            Intent intent = new Intent(this, IngresoOd.class);
            intent.putExtra("vista", "retornos");
            startActivity(intent);
        }else{
            modal.putExtra("error", "Faltan fotos en ods, dirijase al historial y tome fotos alas ods marcadas en rojo (inicie por la ultima od en rojo)");
            startActivity(modal);
        }
    }
    public void goMasivos(View v) {
        Intent intent = new Intent(this, Masivos.class);
        intent.putExtra("vista", "masivos");
        startActivity(intent);
    }
    public void goAgentes(View v) {
        Intent intent = new Intent(this, Agente.class);
        intent.putExtra("vista", "masivos");
        startActivity(intent);
    }
    public void goExitoso(View v) {
        Intent intent = new Intent(this, IngresoOd.class);
        intent.putExtra("vista", "exitoso");
        startActivity(intent);
    }
    public void goEquipo(View v) {
        Intent intent = new Intent(this, Equipo.class);
        intent.putExtra("vista", "equipo");
        startActivity(intent);
    }

    public String getLastLatitude() {
        SQLiteDatabase db = conn.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM certificaciones", null);
            while (cursor.moveToNext()) {
                if (cursor.getString(4) != null && !cursor.getString(4).equals("null") && !cursor.getString(4).equals("") && !cursor.getString(4).equals("0") && !cursor.getString(4).equals("0.0")) {
                    cursor.close();
                    db.close();
                    return cursor.getString(4);
                }
            }
            cursor.close();
            db.close();
            return "FALSE";
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
            db.close();
            return "FALSE";
        }
    }

    public String getLastLongitude() {
        SQLiteDatabase db = conn.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM certificaciones", null);
            while (cursor.moveToNext()) {
                if (cursor.getString(5) != null && !cursor.getString(5).equals("null") && !cursor.getString(5).equals("") && !cursor.getString(5).equals("0") && !cursor.getString(5).equals("0.0")) {
                    cursor.close();
                    db.close();
                    return cursor.getString(5);
                }

            }
            cursor.close();
            db.close();
            return "FALSE";
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
            db.close();
            return "FALSE";
        }
    }

    public void sincronizar() {
        Log.e("state", "va a sincronizar las gestiones");
        SQLiteDatabase db = conn.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM certificaciones WHERE codChofer = " + codigo, null);
            Log.e("sincro eliminado", "paso por aqui y tamaño de sincronizacion es " + cursor.getCount());
            while (cursor.moveToNext()) {
                Log.e("sincro", "paso por aqui y status es  " + cursor.getString(14) + "de od es" + cursor.getString(6));
                if (cursor.getString(13).equals("null")   ) {
                    Log.e("sincro", "entro por validacion de null");
                    Log.e("sincrofoto","od "+cursor.getString(6));
                    Log.e("sincrofoto","tipo "+cursor.getString(16));
                    Log.e("sincrofoto","foto1 "+cursor.getString(9));
                    Log.e("sincrofoto","foto2 "+cursor.getString(10));
                    Log.e("sincrofoto","foto3 "+cursor.getString(11));

                    SimpleTask2 asyncTask = new SimpleTask2();
                    asyncTask.delegate = (AsyncResponse) this;
                    asyncTask.execute(cursor.getString(1), cursor.getString(2), cursor.getString(3),
                            cursor.getString(4), cursor.getString(5), cursor.getString(6),
                            cursor.getString(7), cursor.getString(8), cursor.getString(9),
                            cursor.getString(10), cursor.getString(11), cursor.getString(12),
                            cursor.getString(13), cursor.getString(14), cursor.getString(15),
                            cursor.getString(16), cursor.getString(17), cursor.getString(18),
                            cursor.getString(19), cursor.getString(20), cursor.getString(21), getLastLatitude(), getLastLongitude()
                            , cursor.getString(22), cursor.getString(23), cursor.getString(24), cursor.getString(25),
                            cursor.getString(26));
                    cursor.close();
                    db.close();
                    return;
                } else {
                    Log.e("sincro", "no entro por validacion del null");
                   // return;

                }
            }
            sincronizarAcciones();

        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
            db.close();
        }
    }
    public void sincronizarFoto() {
        Log.e("state", "va a sincronizar las gestiones");
        SQLiteDatabase db = conn.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM certificaciones WHERE codChofer = " + codigo, null);
            Log.e("sincro eliminado", "paso por aqui y tamaño de sincronizacion es " + cursor.getCount());
            while (cursor.moveToNext()) {
                Log.e("sincro", "paso por aqui y status es  " + cursor.getString(14) + "de od es" + cursor.getString(6));
                if (cursor.getString(14).equals("null") && (!cursor.getString(9).equals("FALSE") || !cursor.getString(10).equals("FALSE") || !cursor.getString(11).equals("FALSE"))
                        && cursor.getString(17).equals("TRUE") ) {
                 /*   Log.e("sincro", "entro por validacion de null");
                    Log.e("sincrofoto","od "+cursor.getString(6));
                    Log.e("sincrofoto","tipo "+cursor.getString(16));
                    Log.e("sincrofoto","foto1 "+cursor.getString(9));
                    Log.e("sincrofoto","foto2 "+cursor.getString(10));
                    Log.e("sincrofoto","foto3 "+cursor.getString(11));
                   */
                    SimpleTask1 asyncTask = new SimpleTask1();
                    asyncTask.delegate = (AsyncResponse) this;
                    asyncTask.execute(cursor.getString(1), cursor.getString(2), cursor.getString(3),
                            cursor.getString(4), cursor.getString(5), cursor.getString(6),
                            cursor.getString(7), cursor.getString(8), cursor.getString(9),
                            cursor.getString(10), cursor.getString(11), cursor.getString(12),
                            cursor.getString(13), cursor.getString(14), cursor.getString(15),
                            cursor.getString(16), cursor.getString(17), cursor.getString(18),
                            cursor.getString(19), cursor.getString(20), cursor.getString(21), getLastLatitude(), getLastLongitude()
                            , cursor.getString(22), cursor.getString(23), cursor.getString(24), cursor.getString(25),
                            cursor.getString(26));
                    cursor.close();
                    db.close();
                    return;
                } else {
                    Log.e("sincro", "no entro por validacion del null");
                    // return;

                }
            }


        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
            db.close();
        }
    }
    public void sincronizarAcciones() {
        Log.e("state", "va a sincronizar acciones");
        SQLiteDatabase db = conn.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM acciones WHERE id =" + codigo, null);
          //  Log.e("sincro eliminado", "paso por aqui y tamaño de sincronizacion es " + cursor.getCount());
            while (cursor.moveToNext()) {
               // Log.e("accion tiene",cursor.getString(0)+" ---"+cursor.getString(1)+"---"+cursor.getString(2)+"---"+cursor.getString(3)+"---"+cursor.getString(4)+"---"+cursor.getString(5));
                if (cursor.getString(5).equals("null")) {
                   // Log.e("sincro", "entro por validacion de null");
                    SimpleTask4 asyncTask4 = new SimpleTask4();
                    asyncTask4.delegate = (AsyncResponse) this;
                    asyncTask4.execute(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                            cursor.getString(3), cursor.getString(4));
                    cursor.close();
                    db.close();
                    return;
                } else {
                  //  Log.e("sincro", "no entro por validacion del null");
                }
            }
        } catch (Exception e) {
       //     Log.e("ERROR", e.getMessage());
            db.close();
        }
    }

    public void consultarCertificaciones() {
        SimpleDateFormat dateFormat3 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date3 = new Date();
        String fecha3 = dateFormat3.format(date3);


        SQLiteDatabase db = conn.getReadableDatabase();
        Log.e("state", "Va a consultar");
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM certificaciones WHERE codChofer = " + codigo, null);
           Log.e("state", "tamaño de sincronizacion es " + cursor.getCount());
            while (cursor.moveToNext()) {
                String[] fechaFormat = cursor.getString(12).split(" ");
                Log.e("state", "od es" + cursor.getString(6) + "con status" + cursor.getString(13)+"tipo "+ cursor.getString(16));
                Log.e("cursor", cursor.toString());
                if (cursor.getString(13).equals("null")) {
                    faltaTransmitir++;
                }
                if ( cursor.getString(16).equals("normal") && fecha3.equals(fechaFormat[0])) {
                    entregas++;
                } else if (cursor.getString(16).equals("retiro") && fecha3.equals(fechaFormat[0])) {
                    retirados++;
                } else if (cursor.getString(16).equals("devolucion") && fecha3.equals(fechaFormat[0]))  {
                    devoluciones++;
                }
            }
            entregados.setText(entregas + "");
            retiros.setText(retirados + "");
            devolucion.setText(devoluciones + "");
            total.setText((entregas + devoluciones + retirados) + "");

            if (faltaTransmitir == 0) {
                transmisiones.setText("ESTADO: SINCRONIZADO");
            } else {
                transmisiones.setText("(" + faltaTransmitir + ") NO SINCRONIZADAS");
            }
            cursor.close();
            db.close();
        } catch (SQLiteException ex) {
            conn.onUpgrade(db,1,2);
            consultarCertificaciones();

        }catch (Exception e) {

            Log.e("ERROR", e.getMessage() +" ");
           Log.e("Certificaciones","no encontro al consultar");
        }
    }
    public void consultarAcciones(View v){
        Log.e("Acciones","llego al boton ");
       SQLiteDatabase db = conn.getReadableDatabase();
        Log.e("state", "Va a consultar acciones");
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM acciones WHERE id = " + codigo, null);

            Log.e("state", "tamaño de las acciones es " + cursor.getCount());
            while (cursor.moveToNext()) {
                Log.e("accion tiene",cursor.getString(0)+" ---"+cursor.getString(1)+"---"+cursor.getString(2)+"---"+cursor.getString(3)+"---"+cursor.getString(4)+"---"+cursor.getString(5));
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            Log.e("acciones","no encontro al consultar");

        }
    }
    private void requestPermission(){
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_PHONE_STATE,
        };

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);

        }


    }
    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    public void comprimir(String path){
        String pathImg = compressImage(path);
        Bitmap b = BitmapFactory.decodeFile(pathImg);
        File file = new File(path);
        String []fechaFormat =fechaGestion.split(" ");
        b=mark(b,numerood+" - "+fechaFormat[0]+" - ENTREGA ",130,20,false);
        try {
            FileOutputStream stream = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.JPEG,90,stream);
            stream.flush();
            stream.close();
        } catch (Exception e) {
            Toast.makeText(this, "Error.", Toast.LENGTH_SHORT);
        }
        base64(path);
    }
    public void base64(String path){
        Bitmap bm = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        String encodedString = Base64.encodeToString(b, Base64.NO_WRAP);

        base=encodedString;
        Log.e("state","comprimida foto  1");



    }
    public static Bitmap mark(Bitmap src, String watermark, int alpha, int size, boolean underline) {
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);
        Point location = new Point(10,760);

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#202156"));
        paint.setAlpha(alpha);
        paint.setTextSize(size);
        paint.setAntiAlias(true);
        paint.setUnderlineText(underline);
        canvas.drawText(watermark, location.x, location.y, paint);

        return result;
    }
    public String compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath,options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16*1024];

        try{
            bmp = BitmapFactory.decodeFile(filePath,options);
        }
        catch(OutOfMemoryError exception){
            exception.printStackTrace();

        }
        try{
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        }
        catch(OutOfMemoryError exception){
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float)options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth()/2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));


        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }
    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }
    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(),"Cargoex");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/"+ System.currentTimeMillis() + ".jpg");
        return uriSting;

    }
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }
}

class SimpleTask2 extends AsyncTask<String, Integer, String> {
    public AsyncResponse delegate = null;

    @Override
    protected String doInBackground(String... params) {
        if (!isOnlineNet()) {
            return "false";
        } else {
            String respuesta = "";
            String sql = "http://app.cargoex.cl/app/cargoex/app/gestiones";
            Log.e("sincro", "antes de politicas de seguridad");
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL url = null;
            HttpURLConnection conn;
            Log.e("sincro", "antes de llenar el json");
            String tn, rut, nombreReceptor, nota, multientrega, deccode, canal, lati, longi = "";
            if (params[2] == null || params[2].equals("null")) {
                nombreReceptor = "FALSE";
            } else {
                nombreReceptor = params[2];
            }
            if (params[3] == null || params[3].equals("null") || params[3].equals("")) {
                lati = params[21];
            } else {
                lati = params[3];
            }
            if (params[4] == null || params[4].equals("null") || params[4].equals("")) {
                longi = params[22];
            } else {
                longi = params[4];
            }
            if (params[1] == null || params[1].equals("null")) {
                rut = "FALSE";
            } else {
                rut = params[1];
            }
            if (params[6] == null || params[6].equals("null")) {
                tn = "FALSE";
            } else {
                tn = params[6];
            }
            if (params[7] == null || params[7].equals("null")) {
                nota = "FALSE";
            } else {
                nota = params[7];
            }
            if (params[16] == null || params[16].equals("null")) {
                multientrega = "FALSE";
            } else {
                multientrega = params[16];
            }
            if (params[17] == null || params[17].equals("null")) {
                deccode = "FALSE";
            } else {
                deccode = params[17];
            }
            if (params[20] == null || params[20].equals("null")) {
                canal = "FALSE";
            } else {
                canal = params[20];
            }
            try {
                JSONObject jsonPrueba = new JSONObject();
                //        Log.e("sincro","creo json");
                jsonPrueba.put("COD_CHOFER", params[0]);
                //        Log.e("sincro","paso por 1");
                jsonPrueba.put("NOMBRE", nombreReceptor.toUpperCase());
                //        Log.e("sincro","paso por 2");
                jsonPrueba.put("RUT", rut);
                //        Log.e("sincro","paso por 3");
                jsonPrueba.put("LAT_ORIGEN", "FALSE");
                //        Log.e("sincro","paso por 4");
                jsonPrueba.put("LONG_ORIGEN", "FALSE");
                //        Log.e("sincro","paso por 5");
                jsonPrueba.put("LAT_TERRENO", lati);
                //        Log.e("sincro","paso por 6");
                jsonPrueba.put("LONG_TERRENO", longi);
                //        Log.e("sincro","paso por 7");
                jsonPrueba.put("OD_PAPEL", params[5]);
                //        Log.e("sincro","paso por 8");
                jsonPrueba.put("TN", tn);
                //        Log.e("sincro","paso por 9");
                jsonPrueba.put("NOTA", nota.toUpperCase());
                //        Log.e("sincro","paso por 10");
                jsonPrueba.put("FOTO1", params[8]);
                //        Log.e("sincro","paso por 11");
                jsonPrueba.put("FOTO2", params[9]);
                //        Log.e("sincro","paso por 12");
                jsonPrueba.put("FOTO3", params[10]);
                //        Log.e("sincro","paso por 13");
                jsonPrueba.put("FH_GESTION", params[11]);
                //        Log.e("sincro","paso por 14");
                jsonPrueba.put("COD_ESTADO", params[14]);
                //        Log.e("sincro","paso por 15");
                jsonPrueba.put("TIPO_CERTIFICACION", params[15].toUpperCase());
                //        Log.e("sincro","paso por 16");
                jsonPrueba.put("MULTIENTREGA", multientrega.toUpperCase());
                //        Log.e("sincro","paso por 17");
                jsonPrueba.put("DEC_CODE", deccode);
                //        Log.e("sincro","paso por 18");
                jsonPrueba.put("ID_TELEFONO", params[18]);
                //        Log.e("sincro","paso por 19");
                jsonPrueba.put("CONEXION", params[19].toUpperCase());
                //        Log.e("sincro","paso por 20");
                jsonPrueba.put("CANAL", canal.toUpperCase());
                jsonPrueba.put("ID_CLIENTE", params[23].toUpperCase());
                jsonPrueba.put("ID_SUCURSAL", params[24].toUpperCase());
                jsonPrueba.put("BULTOS", params[25].toUpperCase());
                jsonPrueba.put("TELEFONO", params[26].toUpperCase());
                jsonPrueba.put("MAIL", params[27].toUpperCase());

                //        Log.e("sincro","paso por 21");
                //        Log.e("sincro",jsonPrueba.toString());
                Log.e("sincro", "json llenado ");
                url = new URL(sql);
                Log.e("sincro", "antes de mandar el url ");

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("X-API-KEY", "55IcsddHxiy2E3q653RpYtb");
                Log.e("sincro", "antes de llamar al servicio escritura");

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                Log.e("sincro, JSON ES ", jsonPrueba.toString());
                wr.write(jsonPrueba.toString());
                wr.flush();
                Log.e("sincro", "servicio llamado");

                StringBuilder sb = new StringBuilder();
                int HttpResult = conn.getResponseCode();
                Log.e("sincro", "cargada respuesta");
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    Log.e("sincro", "entro al ok");
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    respuesta = sb.toString();
                    br.close();
                    return respuesta;
                } else {
                    Log.e("sincro", "no entro al ok");
                    System.out.println(conn.getResponseMessage());
                    System.out.println(conn.getInputStream());

                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                    respuesta = sb.toString();
                    br.close();
                    return "false";
                }
            } catch (Exception e) {
                Log.e("sincro", "entro al catch");
                e.printStackTrace();
            }
            return respuesta;
        }
    }

    public boolean isOnlineNet() {

        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");

            int val = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.e("sincro resultado es", result);
        super.onPostExecute(result);
        delegate.processFinish(result,"1");
    }

}
class SimpleTask4 extends AsyncTask<String, Integer, String> {
    public AsyncResponse delegate = null;

    @Override
    protected String doInBackground(String... params) {
        if (!isOnlineNet()) {
            return "false";
        } else {
            String respuesta = "";
            String sql = "http://app.cargoex.cl/app/cargoex/app/acciones";
            Log.e("sincro", "antes de politicas de seguridad");
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL url = null;
            HttpURLConnection conn;
            Log.e("sincro", "antes de llenar el json");

            try {
                JSONObject jsonPrueba = new JSONObject();
                //        Log.e("sincro","creo json");
                jsonPrueba.put("ACCION", params[4]);
                //        Log.e("sincro","paso por 1");
                jsonPrueba.put("COD_CHOFER", params[0]);
                //        Log.e("sincro","paso por 2");
                jsonPrueba.put("FECHA_INGRESO", params[1]);
                //        Log.e("sincro","paso por 3");
                jsonPrueba.put("LATITUD", params[2]);
                //        Log.e("sincro","paso por 4");
                jsonPrueba.put("LONGITUD", params[3]);

                Log.e("sincro", "json llenado ");
                url = new URL(sql);
                Log.e("sincro", "antes de mandar el url ");

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("X-API-KEY", "55IcsddHxiy2E3q653RpYtb");
                Log.e("sincro", "antes de llamar al servicio escritura");

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                Log.e("ACCIONES, JSON ES ", jsonPrueba.toString());
                wr.write(jsonPrueba.toString());
                wr.flush();
                Log.e("sincro", "servicio llamado");

                StringBuilder sb = new StringBuilder();
                int HttpResult = conn.getResponseCode();
                Log.e("sincro", "cargada respuesta");
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    Log.e("sincro", "entro al ok");
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    respuesta = sb.toString();
                    br.close();
                    return respuesta;
                } else {
                    Log.e("sincro", "no entro al ok");
                    System.out.println(conn.getResponseMessage());
                    System.out.println(conn.getInputStream());

                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                    respuesta = sb.toString();
                    br.close();
                    return "false";
                }
            } catch (Exception e) {
                Log.e("sincro", "entro al catch");
                e.printStackTrace();
            }
            return respuesta;
        }
    }

    public boolean isOnlineNet() {

        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");

            int val = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.e("sincro resultado es", result);
        super.onPostExecute(result);
        delegate.processFinish(result,"2");
    }

}
class SimpleTask1 extends AsyncTask<String, Integer, String> {
    public AsyncResponse delegate = null;

    @Override
    protected String doInBackground(String... params) {
        if (!isOnlineNet()) {
            return "false";
        } else {
            String respuesta = "";
            String sql = "http://app.cargoex.cl/app/cargoex/app/gestiones";
            Log.e("sincro", "antes de politicas de seguridad");
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL url = null;
            HttpURLConnection conn;
            Log.e("sincro", "antes de llenar el json");
            String tn, rut, nombreReceptor, nota, multientrega, deccode, canal, lati, longi = "";
            if (params[2] == null || params[2].equals("null")) {
                nombreReceptor = "FALSE";
            } else {
                nombreReceptor = params[2];
            }
            if (params[3] == null || params[3].equals("null") || params[3].equals("")) {
                lati = params[21];
            } else {
                lati = params[3];
            }
            if (params[4] == null || params[4].equals("null") || params[4].equals("")) {
                longi = params[22];
            } else {
                longi = params[4];
            }
            if (params[1] == null || params[1].equals("null")) {
                rut = "FALSE";
            } else {
                rut = params[1];
            }
            if (params[6] == null || params[6].equals("null")) {
                tn = "FALSE";
            } else {
                tn = params[6];
            }
            if (params[7] == null || params[7].equals("null")) {
                nota = "FALSE";
            } else {
                nota = params[7];
            }
            if (params[16] == null || params[16].equals("null")) {
                multientrega = "FALSE";
            } else {
                multientrega = params[16];
            }
            if (params[17] == null || params[17].equals("null")) {
                deccode = "FALSE";
            } else {
                deccode = params[17];
            }
            if (params[20] == null || params[20].equals("null")) {
                canal = "FALSE";
            } else {
                canal = params[20];
            }
            try {
                JSONObject jsonPrueba = new JSONObject();
                //        Log.e("sincro","creo json");
                jsonPrueba.put("COD_CHOFER", params[0]);
                //        Log.e("sincro","paso por 1");
                jsonPrueba.put("NOMBRE", nombreReceptor.toUpperCase());
                //        Log.e("sincro","paso por 2");
                jsonPrueba.put("RUT", rut);
                //        Log.e("sincro","paso por 3");
                jsonPrueba.put("LAT_ORIGEN", "FALSE");
                //        Log.e("sincro","paso por 4");
                jsonPrueba.put("LONG_ORIGEN", "FALSE");
                //        Log.e("sincro","paso por 5");
                jsonPrueba.put("LAT_TERRENO", lati);
                //        Log.e("sincro","paso por 6");
                jsonPrueba.put("LONG_TERRENO", longi);
                //        Log.e("sincro","paso por 7");
                jsonPrueba.put("OD_PAPEL", params[5]);
                //        Log.e("sincro","paso por 8");
                jsonPrueba.put("TN", tn);
                //        Log.e("sincro","paso por 9");
                jsonPrueba.put("NOTA", nota.toUpperCase());
                //        Log.e("sincro","paso por 10");
                jsonPrueba.put("FOTO1", params[8]);
                //        Log.e("sincro","paso por 11");
                jsonPrueba.put("FOTO2", params[9]);
                //        Log.e("sincro","paso por 12");
                jsonPrueba.put("FOTO3", params[10]);
                //        Log.e("sincro","paso por 13");
                jsonPrueba.put("FH_GESTION", params[11]);
                //        Log.e("sincro","paso por 14");
                jsonPrueba.put("COD_ESTADO", params[14]);
                //        Log.e("sincro","paso por 15");
                jsonPrueba.put("TIPO_CERTIFICACION", params[15].toUpperCase());
                //        Log.e("sincro","paso por 16");
                jsonPrueba.put("MULTIENTREGA", multientrega.toUpperCase());
                //        Log.e("sincro","paso por 17");
                jsonPrueba.put("DEC_CODE", deccode);
                //        Log.e("sincro","paso por 18");
                jsonPrueba.put("ID_TELEFONO", params[18]);
                //        Log.e("sincro","paso por 19");
                jsonPrueba.put("CONEXION", params[19].toUpperCase());
                //        Log.e("sincro","paso por 20");
                jsonPrueba.put("CANAL", canal.toUpperCase());
                jsonPrueba.put("ID_CLIENTE", params[23].toUpperCase());
                jsonPrueba.put("ID_SUCURSAL", params[24].toUpperCase());
                jsonPrueba.put("BULTOS", params[25].toUpperCase());
                jsonPrueba.put("TELEFONO", params[26].toUpperCase());
                jsonPrueba.put("MAIL", params[27].toUpperCase());

                //        Log.e("sincro","paso por 21");
                //        Log.e("sincro",jsonPrueba.toString());
                Log.e("sincro", "json llenado ");
                url = new URL(sql);
                Log.e("sincro", "antes de mandar el url ");

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("X-API-KEY", "55IcsddHxiy2E3q653RpYtb");
                Log.e("sincro", "antes de llamar al servicio escritura");

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                Log.e("sincro, JSON ES ", jsonPrueba.toString());
                wr.write(jsonPrueba.toString());
                wr.flush();
                Log.e("sincro", "servicio llamado");

                StringBuilder sb = new StringBuilder();
                int HttpResult = conn.getResponseCode();
                Log.e("sincro", "cargada respuesta");
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    Log.e("sincro", "entro al ok");
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    respuesta = sb.toString();
                    br.close();
                    return respuesta;
                } else {
                    Log.e("sincro", "no entro al ok");
                    System.out.println(conn.getResponseMessage());
                    System.out.println(conn.getInputStream());

                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                    respuesta = sb.toString();
                    br.close();
                    return "false";
                }
            } catch (Exception e) {
                Log.e("sincro", "entro al catch");
                e.printStackTrace();
            }
            return respuesta;
        }
    }

    public boolean isOnlineNet() {

        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");

            int val = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.e("sincro resultado es", result);
        super.onPostExecute(result);
        delegate.processFinish(result,"3");
    }

}