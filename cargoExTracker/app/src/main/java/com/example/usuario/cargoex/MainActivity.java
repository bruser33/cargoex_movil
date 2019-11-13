package com.example.usuario.cargoex;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.cargo.usuario.cargoex.R;

import com.example.usuario.cargoex.entities.Usuario;
import com.example.usuario.cargoex.util.SqliteHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    /*     Button boton = (Button) findViewById(R.id.button);
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               EditText edit = (EditText)findViewById(R.id.rut);
                int numero = Integer.parseInt(edit.getText().toString());

                EditText edit2 = (EditText)findViewById(R.id.verificador);
                String dv= (edit2.getText().toString());
                char dvc=dv.charAt(0);
                Intent intent = new Intent();
                intent.setAction("cl.autentia.operacion.VERIFICAR_IDENTIDAD");
                intent.putExtra("RUT", numero);
                intent.putExtra("DV",dvc);
                intent.putExtra("HIDE_RUT", false);
                intent.putExtra("ORIENTACION", "VERTICAL");
                startActivityForResult(intent, 2);

            }
        });
        */
    }

    public void insertar(View v){

        String sql = "http://app.cargoex.cl/app/api/example/test";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        URL url = null;
        HttpURLConnection conn;

        try {
            url = new URL(sql);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-API-KEY", "xIMdHxtuiy2MBEqRFtRb");
            conn.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String inputLine;

            StringBuffer response = new StringBuffer();

            String json = "";

            while((inputLine = in.readLine()) != null){
                response.append(inputLine);
            }

            json = response.toString();

            JSONObject jsonObject = null;

            jsonObject = new JSONObject(json);
/*
            Toast toast3 =
                    Toast.makeText(getApplicationContext(),
                            "status es "+jsonObject.getString("status")+"mensaje es "+jsonObject.getString("message"), Toast.LENGTH_SHORT);
            toast3.show();

*/
            SqliteHelper conn2 = new SqliteHelper(this,"bd_usuarios",null,1);
            SQLiteDatabase db = conn2.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("id","18");
            values.put("nombre",jsonObject.getString("status"));
            values.put("telefono",jsonObject.getString("message"));

            Long id_result = db.insert("usuarios","18",values);

            Toast toast4 =
                    Toast.makeText(getApplicationContext(),
                            "inserto "+id_result, Toast.LENGTH_SHORT);

            toast4.show();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }



    public void consultar(View v){
        Usuario us = new Usuario();
        SqliteHelper conn = new SqliteHelper(this,"bd_usuarios",null,1);
        SQLiteDatabase db = conn.getWritableDatabase();
        String [] parametros = {"18"};
        String [] campos ={"nombre","telefono"};

        try {
            Cursor cursor =db.query("usuarios",campos,"id = ?",parametros,null,null,null);
            cursor.moveToFirst();
            Toast toast3 =
                    Toast.makeText(getApplicationContext(),
                            "status encontrado es  "+cursor.getString(0)+"message es "+cursor.getString(1), Toast.LENGTH_SHORT);

            toast3.show();
            cursor.close();
        }catch (Exception e){
            Log.e("ERROR",e.getMessage());
            Toast toast3 =
                    Toast.makeText(getApplicationContext(),
                            " no encontrando", Toast.LENGTH_SHORT);

            toast3.show();
        }

    }

    public void actualizar(){
        Usuario us = new Usuario();
        SqliteHelper conn = new SqliteHelper(this,"bd_usuarios",null,1);
        SQLiteDatabase db = conn.getWritableDatabase();
        String [] parametros = {us.getId()};
        ContentValues values = new ContentValues();
        values.put("nombre","carlos");
        values.put("telefono","doble30");
        db.update("usuarios",values,"id = ?",parametros);
        db.close();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to

        if (requestCode == 2) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                Toast toast3 =
                        Toast.makeText(getApplicationContext(),
                                "si entro al ok y trajo datos de la huella", Toast.LENGTH_SHORT);

                toast3.show();
                String result=data.getStringExtra("ESTADO");
                String result3=data.getStringExtra("DESCRIPCION");
                String result4=data.getStringExtra("nombre");
                String result5=data.getStringExtra("codigoAuditoria");
                String result6=data.getStringExtra("fechaNac");
                String result7=data.getStringExtra("apellidos");
                String result8=data.getStringExtra("Idtx");
                String result9=data.getStringExtra("serialNumber");
                String result10=data.getStringExtra("fecha_vencimiento");
                String result11=data.getStringExtra("tipoLector");


                TextView response = findViewById(R.id.texto);
                response.setText("Cargo Ex se ha conectado con ACEPTA, los datos del huellero son : \n" + "Estado de solicitud: "+result+"\n Descripcion: "+result3+"\n \n Nombre: "+result4+"\n Codigo de Auditoria es: "+result5+"\n Fecha de nacimiento es: "+result6+"\n Numero de transaccion: "+result8+"\n Tipo Lector: "+result11);

            }
        }
    }
}
