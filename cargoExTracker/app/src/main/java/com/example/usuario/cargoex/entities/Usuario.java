package com.example.usuario.cargoex.entities;
import com.cargo.usuario.cargoex.R;

public class Usuario {

    public String table="usuarios";
    public String id = "9";
    public String nombre = "Carlo Magno";
    public String telefono = "007";

    public Usuario() {
    }

    public void meh(){

    }
    public String getTable() {
        return this.table;
    }

    public String getId() {
        return this.id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public String getTelefono() {
        return this.telefono;
    }

}
