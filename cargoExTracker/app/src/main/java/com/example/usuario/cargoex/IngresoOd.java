package com.example.usuario.cargoex;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cargo.usuario.cargoex.R;

import com.example.usuario.cargoex.util.AsyncResponse;
import com.example.usuario.cargoex.util.Modal;
import com.example.usuario.cargoex.util.ModalVersion;
import com.example.usuario.cargoex.util.SqliteCertificaciones;
import com.example.usuario.cargoex.util.SqliteHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.zxing.Result;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class IngresoOd extends AppCompatActivity implements AsyncResponse,ZXingScannerView.ResultHandler  {
    private FusedLocationProviderClient client;
    String latitud,longitud,vista="";
    EditText od;
    Intent modal,modalVersion;
    SharedPreferences prefs ;
    String codigo,versionWeb,versionMovil;
    SqliteCertificaciones conn;
    TextView numero ;
    LinearLayout ods;
    int contadorOds ;
    ArrayList <String> listaOd;
    private ZXingScannerView scanner;
    boolean flagCamara = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ingresood);
        listaOd= new ArrayList<String>();
//inicializa
        od=(EditText) findViewById(R.id.ingresoOdd);
        numero=(TextView) findViewById(R.id.od);
        modal= new Intent(this, Modal.class);
        modalVersion = new Intent(this, ModalVersion.class);
        ods=findViewById(R.id.layoutods);
        vista= getIntent().getStringExtra("vista");
        od.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() != 0) {

                    if (vista.equals("devoluciones")) {
                        numero.setText(" NO ENTREGA : " + od.getText().toString());

                    } else if (vista.equals("entregas")) {
                        numero.setText(" ENTREGA : " + od.getText().toString());
                    } else if (vista.equals("retiros")) {
                        numero.setText(" RETIRO : " + od.getText().toString());

                    } else if (vista.equals("retornos")) {
                        numero.setText(" RETORNO : " + od.getText().toString());

                    }

                }
            }
        });
//pongo nombre
        TextView nombre= (TextView) findViewById(R.id.nombre);
        prefs = getSharedPreferences("Preferencias",Context.MODE_PRIVATE);
        String name = prefs.getString("nombre", "");
        codigo=prefs.getString("codigo", "");
        nombre.setText(name);
        //pongo fecha actual en la vista
        TextView fecha = (TextView) findViewById(R.id.fecha);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy ", Locale.getDefault());
        Date date = new Date();
        String fechaHoy = dateFormat.format(date);
        fecha.setText(fechaHoy);
//termina
        conn = new SqliteCertificaciones(this,"bd_certificaciones",null,R.string.versionDB);
        prefs = getSharedPreferences("Preferencias",Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("procesado", "false");
        editor.putString("path1", "false");
        editor.putString("path2","false");
        editor.putString("path3","false");
        editor.putInt("errorh", 0);
        editor.commit();


        //registro en base de datos local la accion
        Log.e("acciones","antes de entrar al codigo de acciones");

        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        Date date2 = new Date();
        String fecha2 = dateFormat2.format(date2);
        Log.e("acciones","antes de entrar al codigo de acciones");


        //requestPermission();
        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(IngresoOd.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }

        client.getLastLocation().addOnSuccessListener(IngresoOd.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //tomo latitud y longitud
                    latitud = location.getLatitude() + "";
                    longitud = location.getLongitude() + "";
                }
            }
        });


        buscarUltimaVersion();
    }

    public void inicializaciones(){
//inicializa
        od=(EditText) findViewById(R.id.ingresoOdd);
        numero=(TextView) findViewById(R.id.od);
        modal= new Intent(this, Modal.class);
        modalVersion = new Intent(this, ModalVersion.class);
        ods=findViewById(R.id.layoutods);
        vista= getIntent().getStringExtra("vista");
        od.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() != 0) {

                    if (vista.equals("devoluciones")) {
                        numero.setText(" NO ENTREGA : " + od.getText().toString());

                    } else if (vista.equals("entregas")) {
                        numero.setText(" ENTREGA : " + od.getText().toString());
                    } else if (vista.equals("retiros")) {
                        numero.setText(" RETIRO : " + od.getText().toString());

                    } else if (vista.equals("retornos")) {
                        numero.setText(" RETORNO : " + od.getText().toString());

                    }

                }
            }
        });
//pongo nombre
        TextView nombre= (TextView) findViewById(R.id.nombre);
        prefs = getSharedPreferences("Preferencias",Context.MODE_PRIVATE);
        String name = prefs.getString("nombre", "");
        codigo=prefs.getString("codigo", "");
        nombre.setText(name);
        //pongo fecha actual en la vista
        TextView fecha = (TextView) findViewById(R.id.fecha);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy ", Locale.getDefault());
        Date date = new Date();
        String fechaHoy = dateFormat.format(date);
        fecha.setText(fechaHoy);
//termina
    }
    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("errorh", 0);
        editor.commit();
    }

    public void Escanear(View view){
        this.abrirScaner();
    }

    public void abrirScaner(){
        this.scanner = new ZXingScannerView(this);
        this.scanner.setResultHandler(this);
        this.flagCamara = true;
        setContentView(scanner);
        scanner.startCamera();
    }
    public void cargarLista(){
        contadorOds=0;
        /*
        Button verificar =new Button (this);
        verificar.setText("VERIFICAR");
        Drawable d = getResources().getDrawable(R.drawable.action);
        verificar.setBackground(d);
        verificar.setPadding(0,5,0,0);
        verificar.setWidth(40);  */
        Log.e("here", this.listaOd.toString());
        this.ods=findViewById(R.id.layoutods);

        // ods.addView(verificar,contadorOds);
        for(int i=0;i<=listaOd.size()-1;i++){
            Log.e("here","recorrio"+listaOd.get(i));
            final TextView textView= new Button(this);
            textView.setText((i+1)+" - "+listaOd.get(i));
            textView.setTextAppearance(this, R.style.item);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1f);
            params.gravity = Gravity.LEFT;
            textView.setLayoutParams(params);
            textView.setId(i);
            if(i%2==0){
                textView.setBackgroundResource(R.drawable.myborder);
            }else{
                textView.setBackgroundResource(R.drawable.mybordergray);
            }
            ods.addView(textView,contadorOds);
            contadorOds++;
        }
    }
    @Override
    public void onBackPressed() {
        Log.e("back",this.flagCamara+"");
        if (this.flagCamara) {
            setContentView(R.layout.ingresood);

        this.inicializaciones();
        this.cargarLista();
            this.flagCamara = false;

        } else{
            Log.e("msj","llego por no");
            super.onBackPressed();
        }
    }
    @Override
    public void handleResult(Result result){
        String dato = result.getText();
        setContentView(R.layout.ingresood);
        scanner.stopCamera();
        this.flagCamara = false;
        this.inicializaciones();
        if(dato.equals("") || Double.parseDouble(dato)<70000){
            modal.putExtra("error", "INGRESA UNA OD VALIDA ");
            startActivity(modal);
            MediaPlayer mediaPlayer;
            mediaPlayer = MediaPlayer.create(this, R.raw.nook);
            mediaPlayer.start();
            this.cargarLista();
        }else if(listaOd.contains(dato) ){
            modal.putExtra("error", "ESTA OD YA SE ENCUENTRA ALMACENADA");
            startActivity(modal);
            MediaPlayer mediaPlayer;
            mediaPlayer = MediaPlayer.create(this, R.raw.nook);
            mediaPlayer.start();
            this.cargarLista();
        }else{
            listaOd.add(dato);
            cargarLista();
//            TextView textView= new TextView(this);
//            textView.setText((contadorOds+1)+" - "+dato);
//            textView.setTextAppearance(this, R.style.item);
//            textView.setPadding(70,0,0,0);
//            if(contadorOds%2==0){
//                textView.setBackgroundResource(R.drawable.myborder);
//            }else{
//                textView.setBackgroundResource(R.drawable.mybordergray);
//            }
//            ods.addView(textView,contadorOds);
//            contadorOds++;
//            od.setText("");
//
            MediaPlayer mediaPlayer;
            mediaPlayer = MediaPlayer.create(this, R.raw.ok);
            mediaPlayer.start();
        }




            //aqui otra vez

    }

    public void limpiezaPorActualizacion(){
        try {
            SQLiteDatabase db = conn.getWritableDatabase();
            String[] parametros = {"null"};
            db.delete("certificaciones", "fechaEnvio != ?", parametros);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }
    public void limpiezaPorActualizacion2(){
        try {
            SQLiteDatabase db = conn.getWritableDatabase();
            String[] parametros = {"null","TRUE"};
            db.delete("certificaciones", "status != ? and multientrega != ?", parametros);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }
    public void buscarUltimaVersion(){
        SimpleTask3 asyncTask3 = new SimpleTask3();
        asyncTask3.delegate = (AsyncResponse) this;
        asyncTask3.execute();

    }

    public void mas (View v){
        if(od.getText().toString().equals("")){
            modal.putExtra("error", "NO PUEDES DEJAR OD VACIA");
            startActivity(modal);
        }else if(!od.getText().toString().equals("") && Double.parseDouble(od.getText().toString())<70000){
            modal.putExtra("error", "INGRESA UNA OD VALIDA ");
            startActivity(modal);
        }
       else if(listaOd.contains(od.getText().toString()) ){
            modal.putExtra("error", "ESTA OD YA SE ENCUENTRA ALMACENADA");
            startActivity(modal);
        }else{
            listaOd.add(od.getText().toString());
            ods.removeAllViews();
            this.cargarLista();
            od.setText("");
        }
    }
    public boolean existsOd(String od){
        SQLiteDatabase db = conn.getReadableDatabase();
        Cursor cursor =db.rawQuery("SELECT * FROM certificaciones",null);
        try {
            while(cursor.moveToNext()){
                if(cursor.getString(6).equals(od)){

                    cursor.close();
                    db.close();
                    return true;
                }else{
                    Log.e("sincro","no entro por validacion del null");
                }
            }
           ;
        }catch (Exception e){
            Log.e("ERROR",e.getMessage());
            db.close();
        }
        cursor.close();
        db.close();
        return false;
    }
    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }
    public boolean ultimaPosicionAccion(){
        String codigo = prefs.getString("codigo", "");
        Log.e("ULTIMA ACCION","llego al boton ");
        SQLiteDatabase db = conn.getReadableDatabase();
        Log.e("ULTIMA ACCION", "Va a consultar acciones");
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM acciones WHERE id = " + codigo, null);
            Log.e("ULTIMA ACCION", "tamaño de las acciones es " + cursor.getCount());
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
    public void normal(View v){
        if(listaOd.size()==0 && od.getText().toString().equals("")) {
            modal.putExtra("error", "NO PUEDES DEJAR OD VACIO  ");
            startActivity(modal);
        }else if(!od.getText().toString().equals("") && Double.parseDouble(od.getText().toString())<70000){
            modal.putExtra("error", "INGRESA UNA OD VALIDA ");
            startActivity(modal);
        }
        else{
            if(!od.getText().toString().equals("")){
            listaOd.add(od.getText().toString());
            TextView textView= new TextView(this);
            textView.setText((contadorOds+1)+" - "+od.getText().toString());
            textView.setTextAppearance(this, R.style.item);
            textView.setPadding(70,0 ,0,0);
            if(contadorOds%2==0){
                textView.setBackgroundResource(R.drawable.myborder);
            }else{
                textView.setBackgroundResource(R.drawable.mybordergray);

            }
            ods.addView(textView,contadorOds);
            contadorOds++;
            od.setText("");
            }
            //registro en base de datos local la accion
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            Date date2 = new Date();
            String fecha2 = dateFormat2.format(date2);
            Log.e("acciones","Registro Acciones");
            /*
            if (ActivityCompat.checkSelfPermission(IngresoOd.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) { }
            client.getLastLocation().addOnSuccessListener(IngresoOd.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        SQLiteDatabase db = conn.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                        Date date2 = new Date();
                        String fecha2 = dateFormat2.format(date2);
                        latitud = location.getLatitude() + "";
                        longitud = location.getLongitude() + "";
                        if (latitud == null || longitud == null || latitud.equals("") || longitud.equals("") || latitud.equals(" ") || longitud.equals(" ")) {
                            if(ultimaPosicionAccion()){
                                values.put("id", codigo);
                                values.put("fechaIngreso", fecha2);
                                values.put("latitud", latitud);
                                values.put("longitud", longitud);
                                values.put("accion", "ingresoOd");
                                values.put("fechaEnvio", "null");
                                Long id_result = db.insert("acciones", codigo, values);
                                db.close();
                            }else if(ultimaPosicionGestion()){

                                values.put("id", codigo);
                                values.put("fechaIngreso", fecha2);
                                values.put("latitud", latitud);
                                values.put("longitud", longitud);
                                values.put("accion", "ingresoOd");
                                values.put("fechaEnvio", "null");
                                Long id_result = db.insert("acciones", codigo, values);
                                db.close();
                            }

                        } else {
                            values.put("id", codigo);
                            values.put("fechaIngreso", fecha2);
                            values.put("latitud", latitud);
                            values.put("longitud", longitud);
                            values.put("accion", "ingresoOd");
                            values.put("fechaEnvio", "null");
                            Long id_result = db.insert("acciones", codigo, values);
                            db.close();

                        }
                    }
                });

                */
            Log.e("vista",vista);
            if(vista.equals("devoluciones")){
                Intent intent = new Intent(this, Devoluciones.class);
                intent.putExtra("od", od.getText().toString());
                intent.putExtra("lista",listaOd);
                intent.putExtra("vista","devolucion");
                startActivityForResult(intent,777);
            }else if(vista.equals("entregas")){
                Intent intent = new Intent(this, Confimacion2.class);
                intent.putExtra("od", od.getText().toString());
                intent.putExtra("lista",listaOd);
                startActivityForResult(intent,777);
            }else if(vista.equals("retiros")){
                Intent intent = new Intent(this, Confirmacion.class);
                intent.putExtra("od", od.getText().toString());
                intent.putExtra("lista",listaOd);
                startActivityForResult(intent,777);
            }else if(vista.equals("retornos")){
                Intent intent = new Intent(this, Devoluciones.class);
                intent.putExtra("od", od.getText().toString());
                intent.putExtra("lista",listaOd);
                intent.putExtra("vista","retorno");
                startActivityForResult(intent,777);
            }else if(vista.equals("exitoso")){
                Intent intent = new Intent(this, Devoluciones.class);
                intent.putExtra("od", od.getText().toString());
                intent.putExtra("lista",listaOd);
                intent.putExtra("vista",vista);
                startActivityForResult(intent,777);
            }

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 777 ) {
            if (resultCode == RESULT_OK) {
                String returnString = data.getStringExtra("procesado");
                if(returnString.equals("true")){
                    File files = new File(Environment.getExternalStorageDirectory().getPath()+"/Cargoex");
                    try {
                        FileUtils.deleteDirectory(files);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Intent output = new Intent();
                    output.putExtra("procesado", "true");
                    setResult(RESULT_OK, output);
                    this.finish();
                }
            }
        }else if (requestCode == 2 ) {
            if(!versionWeb.equals(versionMovil)){
                modalVersion.putExtra("error", "DEBES ACTUALIZAR ALA ULTIMA VERSION");
                startActivityForResult(modalVersion,2);
                limpiezaPorActualizacion();
                limpiezaPorActualizacion2();
            }
        }
    }

    @Override
    public void processFinish(String output,String identificadorServicio) {
        if (output.equals("false")) {
            Log.e("sate", "no hay internet");
        } else {
            try {
                JSONObject jsonObject = new JSONObject(output);
                Log.e("status", "llego del servicio " + output);
                if (jsonObject.getString("success").compareTo("true") == 0 ) {
                    Log.e("VERSION", "pasoooo");

                    JSONArray insercion = jsonObject.getJSONArray("version");
                    versionWeb= insercion.getJSONObject(0).getString("VERSION");
                    Log.e("VERSION",versionWeb);
                    PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
                    versionMovil= packageInfo.versionName;
                    if(!versionWeb.equals(versionMovil)){
                        modalVersion.putExtra("error", "DEBES ACTUALIZAR ALA ULTIMA VERSION");
                        startActivityForResult(modalVersion,2);
                    }
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
class SimpleTask3 extends AsyncTask<String, Integer, String> {
    public AsyncResponse delegate = null;


    @Override
    protected String doInBackground(String... params) {
        if (!isOnlineNet()) {
            return "false";
        } else {
            Log.e("Version","llego a usar el servicio de verificacion");
            String respuesta = "";
            String sql = "http://app.cargoex.cl/app/cargoex/app/versionApk";
            Log.e("Version","PASO POR AQUI1 ");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL url = null;

            HttpURLConnection conn;
            Log.e("Version","PASO POR AQUI2 ");

            try {
                url = new URL(sql);
                conn = (HttpURLConnection) url.openConnection();
                Log.e("Version","PASO POR AQUI3 ");

                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("X-API-KEY", "55IcsddHxiy2E3q653RpYtb");
                Log.e("version", "antes de llamar al servicio de consulta de version");

                StringBuilder sb = new StringBuilder();
                int HttpResult = conn.getResponseCode();
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
                    Log.e("version", "no entro al ok");
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