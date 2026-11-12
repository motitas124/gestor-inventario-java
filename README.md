# Gestor de Inventario — Java

Aplicación de consola para administrar el inventario de un pequeño negocio: registra productos, entradas y ventas, alerta cuando hay que reponer stock y guarda todo en un archivo CSV.

![Java](https://img.shields.io/badge/Java-17+-ED8B00?logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-build-C71A36?logo=apachemaven&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)

## Funcionalidades

- Alta, búsqueda y eliminación de productos por código o nombre.
- Registro de **entradas** y **salidas** con validación de stock (no permite vender más de lo disponible).
- **Historial de movimientos** con fecha y stock resultante.
- **Alertas de reposición** cuando un producto llega a su stock mínimo.
- **Reportes**: valor total del inventario y valor agrupado por categoría.
- **Persistencia en CSV**: los datos se guardan automáticamente y se cargan al iniciar.

```
CODIGO   NOMBRE                   CATEGORIA        PRECIO  STOCK
AL-001   Cafe molido 500g         ALIMENTOS    $     6.75      5  <- REPONER
EL-001   Mouse inalambrico        ELECTRONICA  $    15.90     25
EL-002   Teclado mecanico         ELECTRONICA  $    49.99      4  <- REPONER

Valor total del inventario: $1079.46
Valor por categoria:
  ELECTRONICA  $597.46
  PAPELERIA    $300.00
```

## Arquitectura

El proyecto separa responsabilidades en capas, de modo que la lógica de negocio no depende de la consola ni del formato de archivo:

```
com.rendysilva.inventario
├── App.java                  → Interfaz de consola (menú, lectura de datos)
├── modelo/                   → Producto (inmutable), Categoria, Movimiento (record)
├── servicio/                 → Inventario: reglas de negocio y reportes
└── persistencia/             → RepositorioCsv: guardar / cargar archivo
```

### Decisiones de diseño

- **`BigDecimal` para precios**: `double` produce errores de redondeo con dinero (0.1 + 0.2 ≠ 0.3).
- **`Producto` inmutable**: cada cambio de stock crea una nueva instancia, evitando modificaciones accidentales.
- **Escritura atómica del CSV**: se escribe a un archivo temporal y luego se reemplaza, para no corromper los datos si el programa se cierra a mitad del guardado.
- **Streams y `Collectors.groupingBy`** para los reportes por categoría.
- **Reloj inyectable** (`Clock`) en `Inventario`, para que las pruebas con fechas sean deterministas.

## Cómo ejecutarlo

Requiere Java 17 o superior y Maven.

```bash
mvn test                                   # corre las pruebas unitarias
mvn package                                # genera target/gestor-inventario-1.0.0.jar
java -jar target/gestor-inventario-1.0.0.jar
```

Sin Maven:

```bash
javac -d out $(find src/main -name "*.java")
java -cp out com.rendysilva.inventario.App
```

La primera vez se cargan productos de ejemplo. Los datos se guardan en `datos/inventario.csv`.

## Pruebas

9 pruebas unitarias con JUnit 5 que cubren: códigos duplicados, ventas con stock insuficiente, cantidades inválidas, cálculo de valores con precisión decimal, alertas de reposición y guardado/carga del CSV.

## Autor

**Rendy Silva** — Estudiante de Ingeniería en Software, Universidad de las Américas (UDLA), Quito.
