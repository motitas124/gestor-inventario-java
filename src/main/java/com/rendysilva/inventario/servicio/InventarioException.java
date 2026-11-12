package com.rendysilva.inventario.servicio;

/** Error de negocio del inventario (producto inexistente, stock insuficiente, etc.). */
public class InventarioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InventarioException(String mensaje) {
        super(mensaje);
    }
}
