package com.rendysilva.inventario.persistencia;

import com.rendysilva.inventario.modelo.Categoria;
import com.rendysilva.inventario.modelo.Producto;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Guarda y carga productos en un archivo CSV.
 * Formato: codigo;nombre;categoria;precio;stock;stockMinimo
 * Se usa ';' como separador para no chocar con nombres que contienen comas.
 */
public class RepositorioCsv {

    private static final String CABECERA = "codigo;nombre;categoria;precio;stock;stockMinimo";
    private final Path archivo;

    public RepositorioCsv(Path archivo) {
        this.archivo = archivo;
    }

    public void guardar(Collection<Producto> productos) {
        List<String> lineas = new ArrayList<>();
        lineas.add(CABECERA);
        for (Producto p : productos) {
            lineas.add(String.join(";", p.getCodigo(), p.getNombre().replace(";", ","),
                    p.getCategoria().name(), p.getPrecio().toPlainString(),
                    String.valueOf(p.getStock()), String.valueOf(p.getStockMinimo())));
        }
        try {
            if (archivo.getParent() != null) Files.createDirectories(archivo.getParent());
            // Se escribe primero a un archivo temporal para no corromper los datos si algo falla.
            Path temporal = archivo.resolveSibling(archivo.getFileName() + ".tmp");
            Files.write(temporal, lineas, StandardCharsets.UTF_8);
            Files.move(temporal, archivo, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el inventario", e);
        }
    }

    /** Carga los productos; las lineas mal formadas se reportan y se omiten. */
    public List<Producto> cargar() {
        List<Producto> productos = new ArrayList<>();
        if (!Files.exists(archivo)) return productos;
        try {
            List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
            for (int i = 1; i < lineas.size(); i++) {
                String linea = lineas.get(i).trim();
                if (linea.isEmpty()) continue;
                try {
                    productos.add(parsear(linea));
                } catch (RuntimeException e) {
                    System.err.printf("Linea %d ignorada (%s): %s%n", i + 1, e.getMessage(), linea);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el inventario", e);
        }
        return productos;
    }

    private static Producto parsear(String linea) {
        String[] c = linea.split(";", -1);
        if (c.length != 6) throw new IllegalArgumentException("se esperaban 6 columnas");
        return new Producto(c[0], c[1], Categoria.desdeTexto(c[2]), new BigDecimal(c[3]),
                Integer.parseInt(c[4]), Integer.parseInt(c[5]));
    }
}
