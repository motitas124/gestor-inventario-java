package com.rendysilva.inventario.servicio;

import com.rendysilva.inventario.modelo.Categoria;
import com.rendysilva.inventario.modelo.Movimiento;
import com.rendysilva.inventario.modelo.Producto;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Logica de negocio del inventario. No sabe nada de consola ni de archivos:
 * eso permite probarla de forma aislada y reutilizarla con otra interfaz.
 */
public class Inventario {

    private final Map<String, Producto> productos = new LinkedHashMap<>();
    private final List<Movimiento> movimientos = new ArrayList<>();
    private final Clock reloj;

    public Inventario() {
        this(Clock.systemDefaultZone());
    }

    /** Constructor con reloj inyectable para pruebas deterministas. */
    public Inventario(Clock reloj) {
        this.reloj = reloj;
    }

    public void agregar(Producto producto) {
        if (productos.containsKey(producto.getCodigo())) {
            throw new InventarioException("Ya existe un producto con codigo " + producto.getCodigo());
        }
        productos.put(producto.getCodigo(), producto);
    }

    public Optional<Producto> buscarPorCodigo(String codigo) {
        return Optional.ofNullable(productos.get(normalizar(codigo)));
    }

    public List<Producto> buscarPorNombre(String texto) {
        String t = texto.toLowerCase();
        return productos.values().stream()
                .filter(p -> p.getNombre().toLowerCase().contains(t))
                .toList();
    }

    public Producto eliminar(String codigo) {
        Producto p = productos.remove(normalizar(codigo));
        if (p == null) throw new InventarioException("No existe el producto " + codigo);
        return p;
    }

    public Producto registrarEntrada(String codigo, int cantidad) {
        validarCantidad(cantidad);
        Producto actual = obtener(codigo);
        Producto actualizado = actual.conStock(actual.getStock() + cantidad);
        guardarMovimiento(actualizado, Movimiento.Tipo.ENTRADA, cantidad);
        return actualizado;
    }

    public Producto registrarSalida(String codigo, int cantidad) {
        validarCantidad(cantidad);
        Producto actual = obtener(codigo);
        if (cantidad > actual.getStock()) {
            throw new InventarioException(String.format(
                    "Stock insuficiente de %s: disponible %d, solicitado %d",
                    actual.getNombre(), actual.getStock(), cantidad));
        }
        Producto actualizado = actual.conStock(actual.getStock() - cantidad);
        guardarMovimiento(actualizado, Movimiento.Tipo.SALIDA, cantidad);
        return actualizado;
    }

    public Producto actualizarPrecio(String codigo, BigDecimal nuevoPrecio) {
        Producto actualizado = obtener(codigo).conPrecio(nuevoPrecio);
        productos.put(actualizado.getCodigo(), actualizado);
        return actualizado;
    }

    public Collection<Producto> listar() {
        return Collections.unmodifiableCollection(productos.values());
    }

    public List<Producto> listarOrdenados(Comparator<Producto> criterio) {
        return productos.values().stream().sorted(criterio).toList();
    }

    public List<Producto> productosParaReponer() {
        return productos.values().stream()
                .filter(Producto::necesitaReposicion)
                .sorted(Comparator.comparingInt(Producto::getStock))
                .toList();
    }

    public BigDecimal valorTotal() {
        return productos.values().stream()
                .map(Producto::valorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<Categoria, BigDecimal> valorPorCategoria() {
        return productos.values().stream().collect(Collectors.groupingBy(
                Producto::getCategoria,
                () -> new EnumMap<>(Categoria.class),
                Collectors.reducing(BigDecimal.ZERO, Producto::valorTotal, BigDecimal::add)));
    }

    public List<Movimiento> historial() {
        return Collections.unmodifiableList(movimientos);
    }

    public int cantidadProductos() {
        return productos.size();
    }

    private Producto obtener(String codigo) {
        return buscarPorCodigo(codigo)
                .orElseThrow(() -> new InventarioException("No existe el producto " + codigo));
    }

    private void guardarMovimiento(Producto actualizado, Movimiento.Tipo tipo, int cantidad) {
        productos.put(actualizado.getCodigo(), actualizado);
        movimientos.add(new Movimiento(LocalDateTime.now(reloj), actualizado.getCodigo(),
                tipo, cantidad, actualizado.getStock()));
    }

    private static void validarCantidad(int cantidad) {
        if (cantidad <= 0) throw new InventarioException("La cantidad debe ser mayor que cero");
    }

    private static String normalizar(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }
}
