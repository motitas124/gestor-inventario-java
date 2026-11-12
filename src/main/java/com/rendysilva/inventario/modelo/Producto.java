package com.rendysilva.inventario.modelo;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Producto del inventario. Es inmutable: cada cambio de stock o precio
 * devuelve una nueva instancia, lo que evita modificaciones accidentales.
 * El precio usa BigDecimal porque double no representa bien el dinero.
 */
public final class Producto {

    private final String codigo;
    private final String nombre;
    private final Categoria categoria;
    private final BigDecimal precio;
    private final int stock;
    private final int stockMinimo;

    public Producto(String codigo, String nombre, Categoria categoria,
                    BigDecimal precio, int stock, int stockMinimo) {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("El codigo es obligatorio");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio");
        if (precio == null || precio.signum() < 0) throw new IllegalArgumentException("El precio no puede ser negativo");
        if (stock < 0) throw new IllegalArgumentException("El stock no puede ser negativo");
        if (stockMinimo < 0) throw new IllegalArgumentException("El stock minimo no puede ser negativo");
        this.codigo = codigo.trim().toUpperCase();
        this.nombre = nombre.trim();
        this.categoria = Objects.requireNonNull(categoria);
        this.precio = precio.setScale(2, java.math.RoundingMode.HALF_UP);
        this.stock = stock;
        this.stockMinimo = stockMinimo;
    }

    public Producto conStock(int nuevoStock) {
        return new Producto(codigo, nombre, categoria, precio, nuevoStock, stockMinimo);
    }

    public Producto conPrecio(BigDecimal nuevoPrecio) {
        return new Producto(codigo, nombre, categoria, nuevoPrecio, stock, stockMinimo);
    }

    public boolean necesitaReposicion() {
        return stock <= stockMinimo;
    }

    public BigDecimal valorTotal() {
        return precio.multiply(BigDecimal.valueOf(stock));
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public Categoria getCategoria() { return categoria; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public int getStockMinimo() { return stockMinimo; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Producto p && codigo.equals(p.codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%-8s %-24s %-12s $%9s %6d%s",
                codigo, nombre, categoria, precio, stock, necesitaReposicion() ? "  <- REPONER" : "");
    }
}
