package com.rendysilva.inventario.modelo;

import java.time.LocalDateTime;

/** Registro historico de una entrada o salida de stock. */
public record Movimiento(LocalDateTime fecha, String codigoProducto, Tipo tipo, int cantidad, int stockResultante) {

    public enum Tipo { ENTRADA, SALIDA }

    @Override
    public String toString() {
        return String.format("%s  %-8s %-8s %5d  -> stock %d",
                fecha.withNano(0), codigoProducto, tipo, cantidad, stockResultante);
    }
}
