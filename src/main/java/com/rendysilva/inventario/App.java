package com.rendysilva.inventario;

import com.rendysilva.inventario.modelo.Categoria;
import com.rendysilva.inventario.modelo.Producto;
import com.rendysilva.inventario.persistencia.RepositorioCsv;
import com.rendysilva.inventario.servicio.Inventario;
import com.rendysilva.inventario.servicio.InventarioException;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Scanner;

/** Interfaz de consola. Solo lee datos del usuario y delega en Inventario. */
public class App {

    private final Inventario inventario = new Inventario();
    private final RepositorioCsv repositorio;
    private final Scanner entrada = new Scanner(System.in);

    public App(Path archivo) {
        this.repositorio = new RepositorioCsv(archivo);
    }

    public static void main(String[] args) {
        Path archivo = Path.of(args.length > 0 ? args[0] : "datos/inventario.csv");
        new App(archivo).iniciar();
    }

    void iniciar() {
        repositorio.cargar().forEach(inventario::agregar);
        if (inventario.cantidadProductos() == 0) cargarEjemplo();
        System.out.printf("Inventario cargado: %d productos%n", inventario.cantidadProductos());

        boolean seguir = true;
        while (seguir) {
            mostrarMenu();
            String opcion = leer("Opcion");
            try {
                switch (opcion) {
                    case "1" -> listar(Comparator.comparing(Producto::getCodigo));
                    case "2" -> listar(Comparator.comparing(Producto::valorTotal).reversed());
                    case "3" -> agregarProducto();
                    case "4" -> mostrar(inventario.registrarEntrada(leer("Codigo"), leerEntero("Cantidad")));
                    case "5" -> mostrar(inventario.registrarSalida(leer("Codigo"), leerEntero("Cantidad")));
                    case "6" -> inventario.buscarPorNombre(leer("Texto a buscar")).forEach(System.out::println);
                    case "7" -> reporte();
                    case "8" -> inventario.historial().forEach(System.out::println);
                    case "9" -> mostrar(inventario.eliminar(leer("Codigo")));
                    case "0" -> seguir = false;
                    default -> System.out.println("Opcion no valida");
                }
            } catch (InventarioException | IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
            if (!"0".equals(opcion)) repositorio.guardar(inventario.listar());
        }
        repositorio.guardar(inventario.listar());
        System.out.println("Inventario guardado. ¡Hasta luego!");
    }

    private void mostrarMenu() {
        System.out.println("""

                ===== GESTOR DE INVENTARIO =====
                 1. Listar productos
                 2. Listar por valor en stock
                 3. Agregar producto
                 4. Registrar entrada
                 5. Registrar salida (venta)
                 6. Buscar por nombre
                 7. Reporte general
                 8. Historial de movimientos
                 9. Eliminar producto
                 0. Salir""");
    }

    private void listar(Comparator<Producto> orden) {
        System.out.printf("%-8s %-24s %-12s %10s %6s%n", "CODIGO", "NOMBRE", "CATEGORIA", "PRECIO", "STOCK");
        inventario.listarOrdenados(orden).forEach(System.out::println);
    }

    private void agregarProducto() {
        Producto p = new Producto(
                leer("Codigo"), leer("Nombre"),
                Categoria.desdeTexto(leer("Categoria (ELECTRONICA, PAPELERIA, ALIMENTOS, LIMPIEZA, OTROS)")),
                new BigDecimal(leer("Precio")), leerEntero("Stock inicial"), leerEntero("Stock minimo"));
        inventario.agregar(p);
        mostrar(p);
    }

    private void reporte() {
        System.out.printf("Productos registrados: %d%n", inventario.cantidadProductos());
        System.out.printf("Valor total del inventario: $%s%n", inventario.valorTotal());
        System.out.println("Valor por categoria:");
        inventario.valorPorCategoria().forEach((cat, valor) -> System.out.printf("  %-12s $%s%n", cat, valor));
        System.out.println("Productos que necesitan reposicion:");
        inventario.productosParaReponer().forEach(p -> System.out.println("  " + p));
    }

    private void cargarEjemplo() {
        inventario.agregar(new Producto("EL-001", "Mouse inalambrico", Categoria.ELECTRONICA, new BigDecimal("15.90"), 25, 5));
        inventario.agregar(new Producto("EL-002", "Teclado mecanico", Categoria.ELECTRONICA, new BigDecimal("49.99"), 4, 5));
        inventario.agregar(new Producto("PA-001", "Cuaderno universitario", Categoria.PAPELERIA, new BigDecimal("2.50"), 120, 30));
        inventario.agregar(new Producto("AL-001", "Cafe molido 500g", Categoria.ALIMENTOS, new BigDecimal("6.75"), 8, 10));
        inventario.agregar(new Producto("LI-001", "Desinfectante 1L", Categoria.LIMPIEZA, new BigDecimal("3.20"), 40, 15));
    }

    private void mostrar(Producto p) {
        System.out.println("OK -> " + p);
    }

    private String leer(String etiqueta) {
        System.out.print(etiqueta + ": ");
        return entrada.hasNextLine() ? entrada.nextLine().trim() : "0";
    }

    private int leerEntero(String etiqueta) {
        while (true) {
            try {
                return Integer.parseInt(leer(etiqueta));
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un numero entero valido.");
            }
        }
    }
}
