package org.gabrielleis;

import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;

import javax.swing.*;
import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        File orekitData = new File("./orekit-data");
        DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orekitData));

        //Coordenadas a establecer
        Scanner sc = new Scanner(System.in);

        while (true) {
            try{
                System.out.println("Ingrese las coordenadas de una locación correspondiente con la órbita\n" +
                        "del satélite SENTINEL-2\n");

                System.out.print("Inserte la latitud: ");
                double lat = sc.nextDouble();
                System.out.print("Inserte la longitud: ");
                double lon = sc.nextDouble();

                Interval currentInterval = PositionCalculator.timeInterval(lat, lon, 20);
                CopernicusClient.checkImages(lon, lat, currentInterval);

                System.out.println("Gracias por usar el software.");
            } catch (Exception e){
                System.out.println("Error: Entrada no válida. Por favor, introduzca números.");
                sc.nextLine();
            }

        }
    }
}