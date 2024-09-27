package org.example

/* El fichero cotizacion.csv (que podéis encontrar en la carpeta ficheros) contiene las cotizaciones de las empresas
del IBEX35 con las siguientes columnas: Nombre (nombre de la empresa), Final (precio de la acción al cierre de bolsa),
Máximo (precio máximo de la acción durante la jornada), Mínimo (precio mínimo de la acción durante la jornada), Volumen (Volumen
al cierre de bolsa), Efectivo (capitalización al cierre en miles de euros).*/

import java.io.BufferedReader
import java.io.BufferedWriter
import java.nio.file.Files
import kotlin.io.path.Path
import java.nio.file.Path
import java.nio.file.StandardOpenOption

// Función principal del programa
fun main() {

    // Definimos las rutas del archivo de entrada y salida
    val rutaFileReader = Path("src\\main\\resources\\cotizacion.csv") // Ruta del archivo CSV de entrada
    val rutaFileWriter = Path("src\\main\\resources\\columnas.csv") // Ruta del archivo CSV de salida

    // Organizamos los datos del fichero en un diccionario
    val diccionario = organizarFichero(rutaFileReader)

    // Creamos un nuevo archivo con el formato requerido
    crearFichero(diccionario, rutaFileWriter)

    // Imprimimos el diccionario por consola
    diccionario.forEach { (columna, valores) ->
        println("$columna: $valores") // Imprime cada columna y sus valores
    }
}

// 1. Construir una función reciba el fichero de cotizaciones y devuelva un diccionario con los datos del fichero por columnas.

fun organizarFichero(ruta: Path): MutableMap<String, List<String>> {

    // Inicializamos el diccionario para almacenar los datos organizados por columnas
    val diccionario: MutableMap<String, List<String>> = mutableMapOf()

    // Abrimos el archivo usando BufferedReader
    val br: BufferedReader = Files.newBufferedReader(ruta)

    // Listas para almacenar las líneas del archivo y los nombres de las columnas
    val fileLines = mutableListOf<List<String>>() // Almacena todas las líneas de datos
    val keys = mutableListOf<String>()            // Almacena los nombres de las columnas

    // Usamos el BufferedReader para leer la primera línea y obtener las claves
    br.use { reader ->
        // Leer la primera línea para extraer las claves (nombres de las columnas)
        val primeraLinea = reader.readLine()
        keys.addAll(primeraLinea.split(";")) // Dividimos la línea por ";" y almacenamos en keys

        // Procesamos las líneas restantes
        reader.forEachLine { line ->
            // El regex elimina puntos seguidos de tres dígitos para manejar separadores de miles
            val regex = Regex("""\.\d{3}""") // Expresión regular para eliminar separadores de miles
            val lineaFormateada = line.replace(regex, "").replace(",", ".") // Formatear números
            val lineaSpliteada: List<String> = lineaFormateada.split(";")   // Dividir por ";"
            fileLines.add(lineaSpliteada)   // Agregamos la línea procesada a la lista de líneas
        }
    }

    // Asignamos cada columna con sus respectivos valores en el diccionario
    keys.forEachIndexed { index, key ->
        val valoresColumna = mutableListOf<String>() // Lista para almacenar valores de cada columna
        fileLines.forEach { fila ->
            valoresColumna.add(fila[index])  // Agrega cada valor de la fila correspondiente a la columna
        }
        diccionario[key] = valoresColumna    // Inserta la columna y sus valores en el diccionario
    }

    return diccionario // Retorna el diccionario con los datos organizados
}

/* 2. Construir una función que reciba el diccionario devuelto por la función anterior y cree un fichero en formato csv con el mínimo,
el máximo y la media de dada columna. */

fun crearFichero(diccionario: MutableMap<String, List<String>>, ruta: Path) {

    // Creamos los directorios necesarios en caso de que no existan en la ruta especificada para el fichero.
    Files.createDirectories(ruta.parent)

    /* Abrimos un BufferedWriter (bw) para escribir en el archivo, creando el archivo si no existe.
    Se indica que se va a crear y que se permite escribir (StandardOpenOption.CREATE y WRITE). */

    val bw: BufferedWriter = Files.newBufferedWriter(ruta, StandardOpenOption.CREATE, StandardOpenOption.WRITE)

    // El bloque 'use' asegura que el recurso (BufferedWriter) se cierre correctamente después de su uso, incluso si hay un error.
    bw.use { writer ->

        // Escribimos la cabecera del archivo CSV, que incluye el nombre de la columna, el mínimo, el máximo y la media.
        writer.write("Columna; Mínimo columna; Máximo columna; Media columna:")
        writer.newLine() // Añade una nueva línea para que los datos empiecen en la siguiente línea.

        // Iteramos sobre cada entrada del diccionario, donde 'key' es el nombre de la columna y 'value' es la lista de valores.
        diccionario.forEach { (key, value) ->

            // Condición para omitir la columna "Nombre", ya que no contiene valores numéricos y no se pueden calcular estadísticas.
            if (key != "Nombre") {
                try {
                    /* Convertimos el string en una lista de Doubles. Esto es necesario para poder realizar
                     cálculos numéricos como mínimo, máximo y media. */

                    val doubleList: List<Double> = value.map { it.toDouble() }

                    // Escribimos en el archivo CSV el nombre de la columna, seguido por su mínimo, máximo y media, separados por ";"
                    writer.write("${key};${doubleList.minOrNull()};${doubleList.maxOrNull()};${doubleList.average()}")
                    writer.newLine()  // Añade una nueva línea después de cada fila de datos.

                    /* Capturamos posibles errores en caso de que algún valor no pueda ser convertido a Double.
                    Esto puede ocurrir si, por ejemplo, la columna contiene valores no numéricos. */

                } catch (e: NumberFormatException) {
                    // Imprimimos un mensaje por consola indicando en qué columna ocurrió el error.
                    println("Error al procesar la columna $key: ${e.message}")
                }
            }
        }
    }
}