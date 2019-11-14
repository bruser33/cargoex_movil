package com.example.usuario.cargoex.util;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.cargo.usuario.cargoex.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ModalGestiones extends Activity {
    TableLayout certificaciones;
    SqliteCertificaciones conn;
    String codigo;
    SharedPreferences prefs ;
    String identificador;
    public String extStorageDirectory,path,numero,base,fechaGestion;
    private static final int CAMERA_REQUEST = 1777;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //creo las variables de los objetos graficos a controlar
        setContentView(R.layout.modalgestiones);
        TextView titulo= (TextView) findViewById(R.id.titulo);
        certificaciones=(TableLayout)findViewById(R.id.certificaciones);
        conn = new SqliteCertificaciones(this, "bd_certificaciones", null, R.string.versionDB);
        prefs = getSharedPreferences("Preferencias",Context.MODE_PRIVATE);

        //pongo el titulo con el color
        String vista = getIntent().getStringExtra("vista");
        codigo= getIntent().getStringExtra("codigo");
        titulo.setText(vista.toUpperCase()+" (ULTIMOS 3 DIAS)");
        switch (vista){
            case "entregas":
                titulo.setTextColor(Color.parseColor("#008000"));
                identificador="normal";
                break;
            case "noentregas":
                titulo.setTextColor(Color.parseColor("#FF0000"));
                identificador="devolucion";
                break;
            case "retiros":
                titulo.setTextColor(Color.parseColor("#CC6600"));
                identificador="retiro";
                break;
        }
        //dimensionamiento del modal
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*.9),(int)(height*.85));
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x =0;
        params.y = -20;
        getWindow().setAttributes(params);
        //lleno la tabla
        path="";
        llenarTabla();

    }

    @Override
    public void onResume() {
        super.onResume();
       // Log.e("fotohistorial","llego al resume"+path);

       /* if (!path.equals("")) {
            comprimir(path);


            SQLiteDatabase db = conn.getWritableDatabase();

            String[] parametros = {numero, fechaGestion};
            ContentValues values = new ContentValues();
            values.put("foto1", base);
            db.update("certificaciones", values, "od = ? and fechaIngreso = ? ", parametros);
            db.close();
        }  */
    }
    public void llenarTabla(){
        //capturo ancho y alto del celular
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Log.e("screen","en ancho hay "+width+"en alto hay "+height);


      // Agrego la primera fila de titulos a la tabla
        TableRow rowTitle = new TableRow(this);
        TableLayout.LayoutParams tableRowParamsTitle=
                new TableLayout.LayoutParams
                        (TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
        tableRowParamsTitle.setMargins(8,0,0,0);
        rowTitle.setLayoutParams(tableRowParamsTitle);

        //  row.setLayoutParams((new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)));
        TextView odTitle = new TextView(this);
        odTitle.setText("OD");
        if(width >=720){
            odTitle.setLayoutParams(new TableRow.LayoutParams(180,ViewGroup.LayoutParams.WRAP_CONTENT));
        }else{
            odTitle.setLayoutParams(new TableRow.LayoutParams(150,ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        odTitle.setTextAppearance(this, R.style.item2);
        odTitle.setBackgroundResource(R.drawable.modal2);
        odTitle.setTextSize(13);
        odTitle.setTypeface(null, Typeface.BOLD);
        odTitle.setGravity(Gravity.CENTER);

        TextView fechaTitle = new TextView(this);
        fechaTitle.setText("FECHA");
        if(width >=720){
            fechaTitle.setLayoutParams(new TableRow.LayoutParams(170,ViewGroup.LayoutParams.WRAP_CONTENT));
        }else{
            odTitle.setLayoutParams(new TableRow.LayoutParams(150,ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        fechaTitle.setTextAppearance(this, R.style.item2);
        fechaTitle.setBackgroundResource(R.drawable.modal2);
        fechaTitle.setTextSize(13);
        fechaTitle.setTypeface(null, Typeface.BOLD);
        fechaTitle.setGravity(Gravity.CENTER);

        TextView diaTitle = new TextView(this);
        diaTitle.setText("HORA");
        if(width >=720){
        diaTitle.setLayoutParams(new TableRow.LayoutParams(120,ViewGroup.LayoutParams.WRAP_CONTENT));
        }else{
            diaTitle.setLayoutParams(new TableRow.LayoutParams(80,ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        diaTitle.setTextAppearance(this, R.style.item2);
        diaTitle.setBackgroundResource(R.drawable.modal2);
        diaTitle.setTextSize(13);
        diaTitle.setTypeface(null, Typeface.BOLD);
        diaTitle.setGravity(Gravity.CENTER);

        TextView estadoTitle = new TextView(this);
        estadoTitle.setText("ESTADO");
        if(width >=720) {
            estadoTitle.setLayoutParams(new TableRow.LayoutParams(150, ViewGroup.LayoutParams.WRAP_CONTENT));
        }else{
            estadoTitle.setLayoutParams(new TableRow.LayoutParams(100,ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        estadoTitle.setTextAppearance(this, R.style.item2);
        estadoTitle.setBackgroundResource(R.drawable.modal2);
        estadoTitle.setTextSize(13);
        estadoTitle.setTypeface(null, Typeface.BOLD);
        estadoTitle.setGravity(Gravity.CENTER);

        rowTitle.addView(odTitle);
        rowTitle.addView(fechaTitle);
        rowTitle.addView(diaTitle);
        rowTitle.addView(estadoTitle);

        certificaciones.addView(rowTitle);


        SQLiteDatabase db = conn.getReadableDatabase();
        try {
            final Cursor cursor = db.rawQuery("SELECT * FROM certificaciones WHERE codChofer = " + codigo, null);
            Log.e("tabla","el tamaño total es "+cursor.getCount());
            while (cursor.moveToNext()) {
                Log.e("tabla",cursor.toString());
                Log.e("tabla","El identificacdor es "+cursor.getString(16));
                if(cursor.getString(16).equals(identificador)){

                // obtengo variable de sincronizacion
                String sinc="";
               if (cursor.getString(13).equals("null")){
                   sinc="NO";
               }else{
                   sinc="SINC";
               }
                String []fechaFormat =cursor.getString(12).split(" ");
                String []day = fechaFormat[0].split("/");
                String numeroDia = day[2].substring(2);
                String []hour = fechaFormat[1].split(":");

                TableRow row = new TableRow(this);
                TableLayout.LayoutParams tableRowParams=
                        new TableLayout.LayoutParams
                                (TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
                tableRowParams.setMargins(8,0,0,0);
                row.setLayoutParams(tableRowParams);
                //creo items de la fila y la lleno con valores
                final TextView od = new TextView(this);
                od.setText( cursor.getString(6));
                if(width >=720){
                    od.setLayoutParams(new TableRow.LayoutParams(180,(ViewGroup.LayoutParams.WRAP_CONTENT+100)));
                }else{
                    od.setLayoutParams(new TableRow.LayoutParams(150,(ViewGroup.LayoutParams.WRAP_CONTENT+100)));
                }
            //    Log.e("fotohistorial","od  "+ cursor.getString(6)+ "foto 1"+  cursor.getString(9));
            //    Log.e("fotohistorial","od  "+ cursor.getString(6)+ "foto 2"+  cursor.getString(10));
            //    Log.e("fotohistorial","od  "+ cursor.getString(6)+ "foto 3"+  cursor.getString(11));
                if(cursor.getString(14).equals("null") && ( cursor.getString(9).equals("FALSE") &&  cursor.getString(10).equals("FALSE") &&  cursor.getString(11).equals("FALSE")) &&  cursor.getString(17).equals("TRUE")  &&  !cursor.getString(16).equals("devolucion") ){
                    od.setTextAppearance(this, R.style.item3);
                    Log.e("fotohistorial","oddes "+ od.getText());
                    Log.e("fotohistorial","fecha gestion es "+ cursor.getString(12));
                    Log.e("fotohistorial","foto1 es "+ cursor.getString(9));
                    Log.e("fotohistorial","status "+ od.getText());

                    final String fechaG=cursor.getString(12);
                    fechaGestion=fechaG;
                    od.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.e("fotohistorial","oddes "+ od.getText());

                          StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                            StrictMode.setVmPolicy(builder.build());
                            File files = new File(Environment.getExternalStorageDirectory().getPath(), "Cargoex");
                            if (!files.exists()) {
                                files.mkdirs();
                            }

                            numero=od.getText()+"";
                            Log.e("fotohistorialc","od es"+ numero+ "fecha gestion"+fechaGestion);

                            extStorageDirectory = Environment.getExternalStorageDirectory().getPath() + "/Cargoex";
                            File file = new File(extStorageDirectory, numero + "1.jpg");
                            Uri mImageUri = Uri.fromFile(file);

                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);


                            Log.e("fotohistorialc","comiteado");
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);

                        }
                    });
                }else{
                    od.setTextAppearance(this, R.style.item2);
                }
                od.setBackgroundResource(R.drawable.modal2);
                od.setGravity(Gravity.CENTER);
                od.setTextSize(13);

                TextView fecha = new TextView(this);
                fecha.setText(day[0]+"/"+day[1]+"/"+numeroDia);
                if(width >=720){
                    fecha.setLayoutParams(new TableRow.LayoutParams(170,(ViewGroup.LayoutParams.WRAP_CONTENT+100)));
                 }else{
                    fecha.setLayoutParams(new TableRow.LayoutParams(140,(ViewGroup.LayoutParams.WRAP_CONTENT+100)));
                 }
                fecha.setTextAppearance(this, R.style.item2);
                fecha.setBackgroundResource(R.drawable.modal2);
                fecha.setGravity(Gravity.CENTER);
                fecha.setTextSize(13);

                TextView dia = new TextView(this);
                dia.setText(hour[0]+":"+hour[1]);
                if(width >=720) {
                    dia.setLayoutParams(new TableRow.LayoutParams(80,(ViewGroup.LayoutParams.WRAP_CONTENT+100)));
                }else{
                    dia.setLayoutParams(new TableRow.LayoutParams(50,(ViewGroup.LayoutParams.WRAP_CONTENT+100)));
                }
                dia.setTextAppearance(this, R.style.item2);
                dia.setBackgroundResource(R.drawable.modal2);
                dia.setGravity(Gravity.CENTER);
                dia.setTextSize(13);

                TextView estado = new TextView(this);
                estado.setText(sinc);
                if(width >=720) {
                    estado.setLayoutParams(new TableRow.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT+100));
                }else{
                    estado.setLayoutParams(new TableRow.LayoutParams(70,ViewGroup.LayoutParams.WRAP_CONTENT+100));
                }
                estado.setTextAppearance(this, R.style.item2);
                estado.setBackgroundResource(R.drawable.modal2);
                estado.setGravity(Gravity.CENTER);
                estado.setTextSize(13);

                row.addView(od);
                row.addView(fecha);
                row.addView(dia);
                row.addView(estado);
                certificaciones.addView(row);
            }else  if(cursor.getString(16).equals("biometrica")){
                    String sinc="";
                    if (cursor.getString(13).equals("null")){
                        sinc="NO";
                    }else{
                        sinc="SINC";
                    }
                    String []fechaFormat =cursor.getString(12).split(" ");
                    String []day = fechaFormat[0].split("/");
                    String numeroDia = day[2].substring(2);
                    String []hour = fechaFormat[1].split(":");

                    //creo fila
                    TableRow row = new TableRow(this);
                    TableLayout.LayoutParams tableRowParams=
                            new TableLayout.LayoutParams
                                    (TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
                    tableRowParams.setMargins(8,0,0,0);
                    row.setLayoutParams(tableRowParams);
                    //creo items de la fila y la lleno con valores
                    final TextView od = new TextView(this);
                    od.setText( cursor.getString(6));
                    if(width >=720){
                        od.setLayoutParams(new TableRow.LayoutParams(180,ViewGroup.LayoutParams.WRAP_CONTENT+100));
                    }else{
                        od.setLayoutParams(new TableRow.LayoutParams(150,ViewGroup.LayoutParams.WRAP_CONTENT+100));
                    }

                    if(cursor.getString(14).equals("null") && ( cursor.getString(9).equals("FALSE") &&  cursor.getString(10).equals("FALSE") &&  cursor.getString(11).equals("FALSE")) &&  cursor.getString(17).equals("TRUE") &&  !cursor.getString(16).equals("devolucion")  ){
                        od.setTextAppearance(this, R.style.item3);
                        final String odaux = cursor.getString(6);
                        final String fechaG=cursor.getString(12);
                        fechaGestion=fechaG;
                  //      numero=odaux;
                        od.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.e("fotohistorial","oddes "+ odaux);
                                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                StrictMode.setVmPolicy(builder.build());
                                File files = new File(Environment.getExternalStorageDirectory().getPath(), "Cargoex");
                                if (!files.exists()) {
                                    files.mkdirs();
                                }
                                extStorageDirectory = Environment.getExternalStorageDirectory().getPath() + "/Cargoex";
                                File file = new File(extStorageDirectory, odaux + "1.jpg");
                                Uri mImageUri = Uri.fromFile(file);
                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                                path = extStorageDirectory + "/" + numero + "1.jpg";

                                numero=od.getText()+"";
                                Log.e("fotohistorialc","od es"+ numero+ "fecha gestion"+fechaGestion);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("numerood", od.getText()+"");
                                editor.putString("fechagestion", fechaGestion);

                                editor.commit();
                                Log.e("fotohistorialc","comiteado");

                                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                            }
                        });
                    }else{
                        od.setTextAppearance(this, R.style.item2);
                    }
                    od.setBackgroundResource(R.drawable.modal2);
                    od.setGravity(Gravity.CENTER);
                    od.setTextSize(13);

                    TextView fecha = new TextView(this);
                    fecha.setText(day[0]+"/"+day[1]+"/"+numeroDia);
                    if(width >=720){
                        fecha.setLayoutParams(new TableRow.LayoutParams(170,ViewGroup.LayoutParams.WRAP_CONTENT+100));
                    }else{
                        fecha.setLayoutParams(new TableRow.LayoutParams(140,ViewGroup.LayoutParams.WRAP_CONTENT+100));
                    }
                    fecha.setTextAppearance(this, R.style.item2);
                    fecha.setBackgroundResource(R.drawable.modal2);
                    fecha.setGravity(Gravity.CENTER);
                    fecha.setTextSize(13);

                    TextView dia = new TextView(this);
                    dia.setText(hour[0]+":"+hour[1]);
                    if(width >=720) {
                        dia.setLayoutParams(new TableRow.LayoutParams(80, ViewGroup.LayoutParams.WRAP_CONTENT+100));
                    }else{
                        dia.setLayoutParams(new TableRow.LayoutParams(50,ViewGroup.LayoutParams.WRAP_CONTENT+100));
                    }
                    dia.setTextAppearance(this, R.style.item2);
                    dia.setBackgroundResource(R.drawable.modal2);
                    dia.setGravity(Gravity.CENTER);
                    dia.setTextSize(13);

                    TextView estado = new TextView(this);
                    estado.setText(sinc);
                    if(width >=720) {
                        estado.setLayoutParams(new TableRow.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT+100));
                    }else{
                        estado.setLayoutParams(new TableRow.LayoutParams(70,ViewGroup.LayoutParams.WRAP_CONTENT+100));
                    }
                    estado.setTextAppearance(this, R.style.item2);
                    estado.setBackgroundResource(R.drawable.modal2);
                    estado.setGravity(Gravity.CENTER);
                    estado.setTextSize(13);

                    row.addView(od);
                    row.addView(fecha);
                    row.addView(dia);
                    row.addView(estado);

                    certificaciones.addView(row);

                }

                }



        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
            db.close();
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {

            path = extStorageDirectory + "/" + numero + "1.jpg";
            Intent output = new Intent();
            output.putExtra("path", path);
            output.putExtra("numero", numero);
            output.putExtra("fecha", fechaGestion);
            Log.e("fotohistorialc","despues de fotood es"+ numero+ "fecha gestion"+fechaGestion);

            setResult(RESULT_OK, output);

            this.finish();
        }else {
            Toast toast =
                    Toast.makeText(getApplicationContext(),
                            "Foto no tomada", Toast.LENGTH_SHORT);
            toast.show();
        }
     }
    public void salir(View v){
        Intent output = new Intent();
        output.putExtra("path", "");
        output.putExtra("numero", "");
        output.putExtra("fecha", "");

        setResult(RESULT_OK, output);

        this.finish();
    }



}
