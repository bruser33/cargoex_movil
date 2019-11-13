package com.example.usuario.cargoex.util;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.example.usuario.cargoex.PdfViewer;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.cargo.usuario.cargoex.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class TemplatePDF {
    private Context context;
    private File pdfFile;
    private Document document;
    private PdfWriter pdfWriter;
    private Paragraph paragraph;
    private Font fTitle = new Font(Font.FontFamily.HELVETICA,20,Font.BOLD);
    private Font fSubtitle = new Font(Font.FontFamily.HELVETICA,18,Font.BOLD);
    private Font fText = new Font(Font.FontFamily.HELVETICA,12,Font.BOLD);
    private Font fHighText = new Font(Font.FontFamily.HELVETICA,15,Font.BOLD,BaseColor.RED);
    public TemplatePDF(Context context){
        this.context=context;
    }
    public void openDocument(){
        createFile();
        try{
            document=new Document(PageSize.A4);
            pdfWriter=PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();
        }catch (Exception e){
            Log.e("openDocument",e.toString());
        }
    }
    public void createFile(){
        File folder = new File(Environment.getExternalStorageDirectory().toString(),"PDF");
        if(!folder.exists())
            folder.mkdirs();
        pdfFile=new File(folder,"cargoex.pdf");
    }
    public void closeDocument(){
        document.close();
    }
    public void addMetaData(String tittle,String subject,String author){
        document.addTitle(tittle);
        document.addSubject(subject);
        document.addAuthor(author);
    }
    public void addTitle(String title,String subtitle,String date){
        paragraph=new Paragraph();
        addChildP(new Paragraph(title,fTitle));
        addChildP(new Paragraph(subtitle,fSubtitle));
        addChildP(new Paragraph("Generado",fHighText));
        paragraph.setSpacingAfter(30);
        try {
            document.add(paragraph);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
    private void addChildP(Paragraph child){
        child.setAlignment(Element.ALIGN_CENTER);
        paragraph.add(child);
    }
    public void addParagraph(String text){
        paragraph = new Paragraph(text,fText);
        paragraph.setSpacingAfter(5);
        paragraph.setSpacingBefore(5);
        try {
            document.add(paragraph);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
public void createTable(String[]header, ArrayList <String []>clients ){
        paragraph = new Paragraph();
        paragraph.setFont(fText);
        PdfPTable pdfTable = new PdfPTable(header.length);
        PdfPCell pdfPCell;
        int indexC=0;
        while(indexC<header.length){
            pdfPCell=new PdfPCell(new Phrase(header[indexC++],fSubtitle));
            pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPCell.setBackgroundColor(BaseColor.GREEN);
            pdfTable.addCell(pdfPCell);
        }
        for(int indexR=0;indexR<clients.size();indexR++){
            String [] row =clients.get(indexR);
            for(indexC=0;indexC<clients.size()-1;indexC++){
                pdfPCell=new PdfPCell(new Phrase(row[indexC]));
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setFixedHeight(40);
                pdfTable.addCell(pdfPCell);
            }
        }
        paragraph.add(pdfTable);
    try {
        document.add(paragraph);
    } catch (DocumentException e) {
        e.printStackTrace();
    }
}
public void viewPDF(){
        Intent i = new Intent(context,PdfViewer.class);
        i.putExtra("path",pdfFile.getAbsolutePath());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
}
}
