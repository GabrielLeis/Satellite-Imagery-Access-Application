package org.gabrielleis;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;

/* Clase encargada de los métodos principales para la obtención de las
 * imágenes satelitáles. */
public class CopernicusClient {

    private final static String accessToken = System.getenv("COPERNICUS_TOKEN");

    //Método para descargar las imágenes satelitáles en las coordenadas y tiempo establecido
    public static void downloadFile(String url, String productId) {
        //Ubicación de descarga del archivo
        String userHome = System.getProperty("user.home");
        String fileName = "Sentinel_Imagery_" + productId + ".zip";
        Path downloadPath = Paths.get(userHome, "Downloads", fileName);

        try {
            System.out.println("Iniciando descarga en: " + downloadPath);

            //Se crea el cliente HTTP
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS) //--location-trusted
                    .build();

            //Se crea la petición HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            //Se envía y maneja la respuesta
            HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(downloadPath));

            //Verificación
            if(response.statusCode() == 200){
                System.out.println("Descarga completada exitosamente.");
                System.out.println("Archivo guardado en: " + response.body().toAbsolutePath());

            } else {
                System.err.println("Error en la descarga. Código HTTP: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Ocurrió una excepción durante la conexión o escritura:");
            e.printStackTrace();
        }
    }

    public static String checkImages(double lon, double lat, Interval interval){

        //Se convierte el string a formato ISO-8601
        String endTime   = interval.getEndTime().toInstant().toString();
        String startTime = interval.getStartTime().toInstant().toString();

        System.out.println("Buscando imágenes reales entre:");
        System.out.println("   Inicio: " + startTime);
        System.out.println("   Fin:    " + endTime);

        try {
            String baseUrl = "https://catalogue.dataspace.copernicus.eu/odata/v1/Products";

            //Construcción del Query URI para la colección de productos con Sentinel-2 + Intersección + Fechas Dinámicas
            String filter = "$filter=Collection/Name eq 'SENTINEL-2' " +
                    "and OData.CSC.Intersects(area=geography'SRID=4326;POINT(" + lon + " " + lat + ")') " +
                    "and ContentDate/Start gt " + startTime + " " +
                    "and ContentDate/End lt " + endTime;

            String fullUrl = baseUrl + "?" + filter.replace(" ", "%20");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(fullUrl)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200){
                String json = response.body();

                if(json.contains("Id")){

                    System.out.println("Imágenes encontradas");

                    //Se busca el valor de la respuesta JSON correspondiente con el Id del producto y se obtiene en productId
                    int indexId = json.indexOf("\"Id\"");
                    int colonIndex = json.indexOf(":", indexId);
                    int startQuote = json.indexOf("\"", colonIndex);
                    int endQuote = json.indexOf("\"", startQuote + 1);

                    String productId = json.substring(startQuote + 1, endQuote);

                    System.out.println("ID del Producto: " + productId);

                    //Se construye el endpoint final para la obtención de los datos
                    String downloadUrl = "https://catalogue.dataspace.copernicus.eu/odata/v1/Products(" + productId + ")/$value";
                    System.out.println(downloadUrl);

                    //downloadFile(downloadUrl, productId);

                    return downloadUrl;
                } else {
                     System.out.println("El satélite no pasó en las últimas 24h o no tomó fotos (Cámara apagada/Nubes densas).");
                     return null;
                }
            } else {
                System.out.println("Error API: " + response.statusCode());
                return null;
            }

        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
