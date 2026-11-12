package com.rendysilva.inventario.modelo;

public enum Categoria {
    ELECTRONICA,
    PAPELERIA,
    ALIMENTOS,
    LIMPIEZA,
    OTROS;

    public static Categoria desdeTexto(String texto) {
        try {
            return Categoria.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTROS;
        }
    }
}
