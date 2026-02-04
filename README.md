# 🛰️ Satellite Imagery Access Application

![Java](https://img.shields.io/badge/Java-17%2B-orange) ![Orekit](https://img.shields.io/badge/Physics-Orekit-blue) ![License](https://img.shields.io/badge/License-MIT-green)

Herramienta de línea de comandos (CLI) desarrollada en Java que puentea la brecha entre la mecánica orbital teórica y la adquisición de datos reales. Calcula ventanas de visibilidad precisas de satélites (Sentinel-2) sobre una Estación Terrena o ROI y automatiza la descarga de productos desde el ecosistema Copernicus Dataspace.

## 🚀 Funcionalidades Clave

* **Cálculo de Mecánica Orbital:** Utiliza la librería [Orekit](https://www.orekit.org/) para propagar órbitas y detectar eventos de visibilidad ("Rising" y "Setting") en tiempo real, en lugar de depender de bases de datos estáticas.
* **Gestión Eficiente de Memoria (I/O):** Implementa `java.net.http.HttpClient` con manejo de `InputStreams` para descargar productos masivos (+1GB) directamente a disco, evitando desbordamientos de memoria RAM (`OutOfMemoryError`).
* **Lógica de Negocio Inteligente:** Transforma los eventos físicos de visibilidad en consultas OData compatibles, aplicando buffers temporales para compensar las diferencias entre el paso del satélite y el tiempo de ingestión del producto.
* **Portabilidad:** Detección automática del sistema operativo y gestión de rutas relativas al directorio de usuario (`user.home`) para compatibilidad entre Windows, macOS y Linux.

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java (JDK 17+)
* **Dinámica Espacial:** Orekit Library
* **Red:** `java.net.http.HttpClient` (Soporte nativo HTTP/2)
* **Parsing:** JSON (Jackson/Gson) & Protocolo OData
* **Build System:** Maven / Gradle

## ⚙️ Configuración (Requisito Previo)

Esta aplicación requiere datos físicos (parámetros de orientación terrestre, saltos de segundos UTC-TAI, efemérides) para inicializar el contexto de Orekit.

1.  **Descargar datos:** Obtén el archivo `orekit-data.zip` más reciente desde el [Orekit](https://www.orekit.org/site-orekit-13.1.2/downloads.html).
2.  **Instalar:**
    * Descomprime el archivo.
    * Renombra la carpeta resultante a `orekit-data`.
    * Mueve la carpeta a tu directorio raíz de usuario:
        * **Windows:** `C:\Users\TuUsuario\orekit-data`
        * **macOS/Linux:** `/Users/TuUsuario/orekit-data` o `/home/TuUsuario/orekit-data`

> **Nota:** La aplicación buscará automáticamente esta carpeta al iniciarse. Si no existe, la ejecución se detendrá con un error explicativo.

## 📦 Ejecución

### Desde el JAR (Recomendado)
Descarga la última versión desde la sección "Releases" de este repositorio y ejecuta:

java -jar Satellite-Imagery-Access-Application.jar
