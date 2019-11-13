package com.example.usuario.cargoex.util;
import com.cargo.usuario.cargoex.R;

public class SpinnerModel {
    public String nombre;

    public SpinnerModel(String nombre){
        this.nombre=nombre;
    }
    public void setNombre(String nombre){
        this.nombre=nombre;
    }
    public String getNombre(){
        return this.nombre;
    }
}
