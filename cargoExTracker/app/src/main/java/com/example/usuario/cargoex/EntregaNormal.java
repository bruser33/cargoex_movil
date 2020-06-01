package com.example.usuario.cargoex;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.cargo.usuario.cargoex.R;
import com.example.usuario.cargoex.util.Modal;
import com.example.usuario.cargoex.util.Modal2;
import com.example.usuario.cargoex.util.SqliteCertificaciones;
import com.example.usuario.cargoex.util.SqliteHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class EntregaNormal extends AppCompatActivity {
    Intent modal,modal2;
    boolean preciso ;
    private FusedLocationProviderClient client;
    SharedPreferences prefs ;
    EditText nombreCliente, rutCliente, dv,comentarios;
    String numero,base1,base2,base3,latitud,longitud,fechaGestion="";
    Button foto1,foto2,foto3;
    boolean f1,f2,f3,f1t,f2t,f3t =false;
    ArrayList <String> lista;
    private static final int CAMERA_REQUEST = 1777;
    SqliteCertificaciones conn;
    ImageView camera1,camera2,camera3;
    int errorh;
    public String extStorageDirectory1,extStorageDirectory2,extStorageDirectory3,codigo,rut,path1,path2,path3,pathaux1,pathaux2,pathaux3="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entreganormal);
        modal2= new Intent(this, Modal2.class);
        preciso=true;
        //pongo nombre
        TextView nombre= (TextView) findViewById(R.id.nombre);
        prefs = getSharedPreferences("Preferencias",Context.MODE_PRIVATE);
        String name = prefs.getString("nombre", "");
        codigo=prefs.getString("codigo", "");
        rut=prefs.getString("rut", "");
        nombre.setText(name);
        errorh=prefs.getInt("errorh",0);
        conn = new SqliteCertificaciones(this,"bd_certificaciones",null,R.string.versionDB);
        camera1=(ImageView)findViewById(R.id.camera1);
        camera2=(ImageView)findViewById(R.id.camera2);
        camera3=(ImageView)findViewById(R.id.camera3);
        pathaux1=prefs.getString("path1", "");
        pathaux2=prefs.getString("path2", "");
        pathaux3=prefs.getString("path3", "");
        nombreCliente=(EditText) findViewById(R.id.nombreCliente);
        rutCliente=(EditText)findViewById(R.id.rutCliente);
        dv=(EditText)findViewById(R.id.dv);
        modal= new Intent(this, Modal.class);
        foto1=(Button)findViewById(R.id.foto1);
        foto2=(Button)findViewById(R.id.foto2);
        foto3=(Button)findViewById(R.id.foto3);
        comentarios=(EditText)findViewById(R.id.comentarios);
        if(!pathaux1.equals("false")){
            foto1.setTextColor(Color.parseColor("#00b200"));
            foto1.setText("FOTO TOMADA");
            f1t=true;
            path1=pathaux1;
        }
        if(!pathaux2.equals("false")){
            foto2.setTextColor(Color.parseColor("#00b200"));
            foto2.setText("FOTO TOMADA");
            f2t=true;
            path2=pathaux2;
        }
        if(!pathaux3.equals("false")){
            foto3.setTextColor(Color.parseColor("#00b200"));
            foto3.setText("FOTO TOMADA");
            f3t=true;
            path3=pathaux3;
        }
        TextView fecha = (TextView) findViewById(R.id.fecha);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy ", Locale.getDefault());
        Date date = new Date();
        String fechaHoy = dateFormat.format(date);
        fecha.setText(fechaHoy);
        TextView od = (TextView)findViewById(R.id.od);
        lista=getIntent().getStringArrayListExtra("lista");
        Log.e("lista tamaño ",lista.size()+"");
        if(lista.size()==1 ){
            numero =lista.get(0);
            od.setText("OD: "+numero);
        }else{
            od.setVisibility(View.INVISIBLE);
            foto1.setVisibility(View.INVISIBLE);
            foto2.setVisibility(View.INVISIBLE);
            foto3.setVisibility(View.INVISIBLE);
            camera1.setVisibility(View.INVISIBLE);
            camera2.setVisibility(View.INVISIBLE);
            camera3.setVisibility(View.INVISIBLE);
        }

        //registtro accion
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        Date date2 = new Date();
        String fecha2 = dateFormat2.format(date2);
        fechaGestion = dateFormat2.format(date2);
        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(EntregaNormal.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        client.getLastLocation().addOnSuccessListener(EntregaNormal.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //tomo latitud y longitud
                    latitud = location.getLatitude() + "";
                    longitud = location.getLongitude() + "";
                }
            }
        });
        String rutIngresado= getIntent().getStringExtra("rutIngresado");
        String dvIngresado= getIntent().getStringExtra("dvIngresado");
        if(rutIngresado!=null||dvIngresado!=null){
            rutCliente.setText(rutIngresado);
            dv.setText(dvIngresado);
        }
    }
    public boolean numeroVerificador(String s){
        try{
            int i = Integer.parseInt(s);
            return true;
        }catch (NumberFormatException n){
            if(s.equals("K")||s.equals("k")){
                return true;
            }
            return false;
        }
    }
    @Override
    protected void onResume() {

        super.onResume();
        Log.e("State ","resumida");
        if(getLocationMode()!=3){
            modal.putExtra("error", "DEBES TENER ACTIVO TU GPS EN ALTA PRESICION");
            startActivityForResult(modal,2);
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
        new java.util.Timer().schedule(
                new java.util.TimerTask(){
                    @Override
                    public void run() {
                        Log.e("state","pausa");
                    }
                },2000
        );
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            if(f1){
                foto1.setTextColor(Color.parseColor("#00b200"));
                foto1.setText("FOTO TOMADA");
                f1t=true;
                f1=false;
                path1=extStorageDirectory1+"/"+numero+"1.jpg";
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("path1", path1);
                editor.commit();
            }
            if(f2){
                // foto2.setBackgroundResource(R.mipmap.botonverde);
                foto2.setTextColor(Color.parseColor("#00b200"));
                foto2.setText("FOTO TOMADA");
                f2t=true;
                f2=false;
                path2=extStorageDirectory2+"/"+numero+"2.jpg";
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("path2", path2);
                editor.commit();
                //  comprimir(extStorageDirectory2+"/"+numero+"2.jpg",2);
            }
            if(f3){
                //foto3.setBackgroundResource(R.mipmap.botonverde);
                foto3.setTextColor(Color.parseColor("#00b200"));
                foto3.setText("FOTO TOMADA");
                f3=false;
                f3t=true;
                path3=extStorageDirectory3+"/"+numero+"3.jpg";
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("path3", path3);
                editor.commit();
                //  comprimir(extStorageDirectory3+"/"+numero+"3.jpg",3);

            }


     /*       Bitmap bm = BitmapFactory.decodeFile(extStorageDirectory+"/"+numero+".jpg");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 3, baos); //bm is the bitmap object
            byte[] b = baos.toByteArray();
            String encodedString = Base64.encodeToString(b, Base64.DEFAULT);
            EditText coment =(EditText)findViewById(R.id.comentarios);
            coment.setText("codifico"+encodedString);
            Toast toast4 =
                    Toast.makeText(getApplicationContext(),
                            "img base64 es "+encodedString, Toast.LENGTH_SHORT);
            toast4.show();
*/
        }else if (requestCode == 1 ) {
            if (resultCode == RESULT_OK) {
                String returnString = data.getStringExtra("status");
                if(returnString.equals("true")){
                    crearCertificaciones();
                }
            }
        } else if (requestCode == 2 ) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

        }else if (requestCode == 4 ) {
            Intent output = new Intent();
            output.putExtra("procesado", "true");
            setResult(RESULT_OK, output);
            this.finish();
        }
        else{
            f1=false;
            f2=false;
            f3=false;
            Toast toast3 =
                    Toast.makeText(getApplicationContext(),
                            "Foto no tomada", Toast.LENGTH_SHORT);
            toast3.show();
        }
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
    public void ClickCamera1(View View) {
//        File files = new File(Environment.getExternalStorageDirectory().getPath(),"Cargoex");
//        if (!files.exists()) {
//            files.mkdirs();
//        }
//        extStorageDirectory1 = Environment.getExternalStorageDirectory().getPath() +"/Cargoex";
//        File file = new File(extStorageDirectory1, numero + "1.jpg");
//        Uri mImageUri = Uri.fromFile(file);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder(); StrictMode.setVmPolicy(builder.build());
        extStorageDirectory1 = Environment.getExternalStorageDirectory().getPath() ;
        File files = new File(extStorageDirectory1,numero + "1.jpg");
        Uri mImageUri = Uri.fromFile(files);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        f1=true;
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }
    public void ClickCamera2(View View) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder(); StrictMode.setVmPolicy(builder.build());

//        File files = new File(Environment.getExternalStorageDirectory().getPath(),"Cargoex");
//        if (!files.exists()) {
//            files.mkdirs();
//        }
        extStorageDirectory2 = Environment.getExternalStorageDirectory().getPath();
        File file = new File(extStorageDirectory2, numero + "2.jpg");
        Uri mImageUri = Uri.fromFile(file);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        f2=true;
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }
    public void ClickCamera3(View View) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder(); StrictMode.setVmPolicy(builder.build());
//        File files = new File(Environment.getExternalStorageDirectory().getPath(),"Cargoex");
//        if (!files.exists()) {
//            files.mkdirs();
//        }
        extStorageDirectory3 = Environment.getExternalStorageDirectory().getPath();
        File file = new File(extStorageDirectory3, numero + "3.jpg");
        Uri mImageUri = Uri.fromFile(file);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        f3=true;
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }
    public void base64(String path, int id){
        Bitmap bm = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        String encodedString = Base64.encodeToString(b, Base64.NO_WRAP);
        if(id==1){
            base1=encodedString;
            Log.e("state","comprimida foto  1");
        }
        if(id==2){
            base2=encodedString;
            Log.e("state","comprimida foto  2");
        }
        if(id==3){
            base3=encodedString;
            Log.e("state","comprimida foto  3");
        }

    }
    public void comprimir(String path,int id){
        String pathImg = compressImage(path);
        Bitmap b = BitmapFactory.decodeFile(pathImg);
        File file = new File(path);
        String []fechaFormat =fechaGestion.split(" ");
        b=mark(b,lista.get(0)+" - "+fechaFormat[0]+" - ENTREGA ",130,20,false);
        try {
            FileOutputStream stream = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.JPEG,100,stream);
            stream.flush();
            stream.close();
        } catch (Exception e) {
            Toast.makeText(this, "Error.", Toast.LENGTH_SHORT);
        }
        base64(path,id);
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
    public void entregar (View view){
        int fallas = 0 ;
        if(!f1t){
            fallas ++;
        }
        if(!f2t){
            fallas ++;
        }
        if(!f3t){
            fallas ++;
        }
        if(getLocationMode()!=3){
            modal.putExtra("error", "DEBES TENER ACTIVO TU GPS EN ALTA PRESICION");
            startActivityForResult(modal,2);
        }else if(codigo.equals("0")){
            modal.putExtra("error", "Android borro tu sesion , debes iniciar sesion denuevo para continuar");
            startActivityForResult(modal,4);
        }
        else if(nombreCliente.getText().toString().equals("")|| dv.getText().toString().equals("")|| rutCliente.getText().toString().equals("")){
            modal.putExtra("error", "Ingrese nombre, Rut o Dv");
            startActivity(modal);
        }else if(rutCliente.length()>8 ||rutCliente.length()<5||!numeroVerificador(dv.getText().toString())){
            modal.putExtra("error", "Ingrese Formato valido de rut ej: 26208287-3");
            startActivity(modal);
        } else if(!f1t && !f2t && !f3t  && lista.size()==1){
            modal.putExtra("error", "DEBES TOMAR ALMENOS UNA FOTO");
            startActivity(modal);
        }
        /*else if(!preciso  && fallas >=2  && lista.size()==1){
            modal.putExtra("error", "DEBES TOMAR ALMENOS DOS FOTOS DEBIDO A GPS IMPRECISO");
            startActivity(modal);
        }*/
        else{
            if(f1t){
                comprimir(path1,1);
            }
            if(f2t){
                comprimir(path2,2);
            }
            if(f3t){
                comprimir(path3,3);
            }
            modal2.putExtra("error", "¿Esta seguro de hacer la entrega?");
            startActivityForResult(modal2,1);
        }
    }
    public int getLocationMode(){
        try {
            return Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public void crearCertificaciones(){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("path1", "false");
        editor.putString("path2","false");
        editor.putString("path3","false");
        editor.commit();
        int fallas = 0 ;
        if(!f1t){
            fallas ++;
        }
        if(!f2t){
            fallas ++;
        }
        if(!f3t){
            fallas ++;
        }
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            modal.putExtra("error", "DEBES TENER ACTIVO TU GPS");
            startActivity(modal);
        }else{
        if(latitud==null || longitud==null ||latitud.equals("")||longitud.equals("")){
            preciso=false;
            if(!ultimaPosicionAccion()){
                if(!ultimaPosicionGestion()&& fallas >=2){
                    modal.putExtra("error", "TIENES GPS IMPRECISO POR MALA SEÑAL DE INTERNET, FAVOR TOMA AGREGA UNA FOTO DEL DOMICILIO PARA COMPROBAR TU LLEGADA AL LUGAR");
                    startActivity(modal);
                    return;
                }
            }
        }
        String android_id = Build.SERIAL;
        String net="";
        if (isNetDisponible()){
            net="true";
        }else{
            net="false";
        }
        if(base1==null){
            Log.e("state","foto uno nullo");
            base1="FALSE";
        }
        if(base2==null){
            Log.e("state","foto dos nullo");
            base2="FALSE";
        }
        if(base3==null){
            Log.e("state","foto tres nullo");
            base3="FALSE";
        }
        SqliteCertificaciones conn2 = new SqliteCertificaciones(this, "bd_certificaciones", null, R.string.versionDB);
        SQLiteDatabase db = conn2.getWritableDatabase();
        Log.e("multientrega","hara 10 multi entregas");
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        Date date2 = new Date();
        String fecha2 = fechaGestion ;
        String []fechaFormat =fecha2.split(" ");
        Log.e("erorh","error h es "+errorh);
        String comentario= comentarios.getText().toString().replace("(","");
        comentario= comentario.replace(")","");

            for(String s:lista){
            ContentValues values = new ContentValues();
            values.put("id", codigo);
            values.put("codChofer", codigo);
            values.put("rut", rutCliente.getText().toString()+"-"+dv.getText().toString());
            values.put("nombreReceptor", nombreCliente.getText().toString());
            values.put("latitud", latitud);
            values.put("longitud", longitud);
            values.put("od", s);
            values.put("tn", "0");
            if (errorh==0) {
                    values.put("nota",comentario);
            }else{
                    values.put("nota",comentario+" ("+errorh+")");
                }
            values.put("foto1", base1);
            values.put("foto2", base2);
            values.put("foto3", base3);
            values.put("fechaIngreso", fecha2);
            values.put("fechaEnvio", "null");
            values.put("status", "null");
            values.put("codEstado", "0");
            values.put("tipoCertificacion", "normal");
            if(lista.size()>1){
                values.put("multientrega", "TRUE");
            }else{
                values.put("multientrega", "null");
            }
            values.put("decCode", "null");
            values.put("idTelefono", android_id);
            values.put("coneccion", net);
            values.put("canal", "null");
            values.put("idCliente", "false");
            values.put("idSucursal", "false");
            values.put("bultos","false");
            values.put("telefono","false");
            values.put("mail", "false");
            values.put("dia", fechaFormat[0]);
            Long id_result = db.insert("certificaciones", codigo, values);
        }
           /* ContentValues values2 = new ContentValues();
            values2.put("id", codigo);
            values2.put("fechaIngreso", fecha2);
            values2.put("latitud", latitud);
            values2.put("longitud", longitud);
            values2.put("accion", "entregaNormal");
            values2.put("fechaEnvio", "null");

            Long id_result = db.insert("acciones", codigo, values2);  */
            db.close();
        Intent output = new Intent();
        output.putExtra("procesado", "true");
        setResult(RESULT_OK, output);
        this.finish();
        }
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
    private boolean isNetDisponible() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }

    public Boolean isOnlineNet(){
        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");

            int val           = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    }
