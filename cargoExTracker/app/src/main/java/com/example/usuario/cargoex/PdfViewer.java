package com.example.usuario.cargoex;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.cargo.usuario.cargoex.R;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class PdfViewer extends AppCompatActivity {
   private PDFView pdfView;
   private File file;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdfviewer);
        pdfView=(PDFView)findViewById(R.id.pdf);
        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            file=new File (bundle.getString("path",""));
        }
        pdfView.fromFile(file)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .enableAntialiasing(true)
                .load();
    }
}
