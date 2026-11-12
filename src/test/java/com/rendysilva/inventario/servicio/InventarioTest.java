package com.rendysilva.inventario.servicio;

import com.rendysilva.inventario.modelo.Categoria;
import com.rendysilva.inventario.modelo.Movimiento;
import com.rendysilva.inventario.modelo.Producto;
import com.rendysilva.inventario.persistencia.RepositorioCsv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventarioTest {

    private Inventario inventario;

    @BeforeEach
    void preparar() {
        inventario = new Inventario();
        inventario.agregar(new Producto("A1", "Lapiz", Categoria.PAPELERIA, new BigDecimal("0.50"), 100, 20));
        inventario.agregar(new Producto("B2", "Audifonos", Categoria.ELECTRONICA, new BigDecimal("25.00"), 3, 5));
    }

    @Test
    void noPermiteCodigosDuplicados() {
        Producto repetido = new Producto("a1", "Otro", Categoria.OTROS, BigDecimal.ONE, 1, 0);
        assertThrows(InventarioException.class, () -> inventario.agregar(repetido));
    }

    @Test
    void entradaYSalidaActualizanStockYRegistranHistorial() {
        inventario.registrarEntrada("A1", 10);
        Producto p = inventario.registrarSalida("a1", 30);
        assertEquals(80, p.getStock());
        assertEquals(2, inventario.historial().size());
        assertEquals(Movimiento.Tipo.SALIDA, inventario.historial().get(1).tipo());
    }

    @Test
    void noPermiteVenderMasDeLoDisponible() {
        InventarioException e = assertThrows(InventarioException.class,
                () -> inventario.registrarSalida("B2", 4));
        assertTrue(e.getMessage().contains("Stock insuficiente"));
        assertEquals(3, inventario.buscarPorCodigo("B2").orElseThrow().getStock());
    }

    @Test
    void rechazaCantidadesNoPositivas() {
        assertThrows(InventarioException.class, () -> inventario.registrarEntrada("A1", 0));
        assertThrows(InventarioException.class, () -> inventario.registrarSalida("A1", -5));
    }

    @Test
    void calculaValorTotalConPrecision() {
        // 100 * 0.50 + 3 * 25.00 = 125.00
        assertEquals(new BigDecimal("125.00"), inventario.valorTotal());
    }

    @Test
    void detectaProductosParaReponer() {
        List<Producto> reponer = inventario.productosParaReponer();
        assertEquals(1, reponer.size());
        assertEquals("B2", reponer.get(0).getCodigo());
    }

    @Test
    void validaDatosDelProducto() {
        assertThrows(IllegalArgumentException.class,
                () -> new Producto("X", "Malo", Categoria.OTROS, new BigDecimal("-1"), 1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new Producto(" ", "Sin codigo", Categoria.OTROS, BigDecimal.ONE, 1, 0));
    }

    @Test
    void eliminarProductoInexistenteLanzaError() {
        inventario.eliminar("A1");
        assertFalse(inventario.buscarPorCodigo("A1").isPresent());
        assertThrows(InventarioException.class, () -> inventario.eliminar("A1"));
    }

    @Test
    void guardaYCargaDesdeCsv() throws IOException {
        Path temporal = Files.createTempDirectory("inv").resolve("inventario.csv");
        RepositorioCsv repo = new RepositorioCsv(temporal);
        repo.guardar(inventario.listar());

        List<Producto> cargados = repo.cargar();
        assertEquals(2, cargados.size());
        assertEquals(new BigDecimal("25.00"), cargados.get(1).getPrecio());
        assertEquals(Categoria.ELECTRONICA, cargados.get(1).getCategoria());
    }
}
