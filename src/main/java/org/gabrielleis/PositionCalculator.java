package org.gabrielleis;

import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.propagation.events.EventsLogger;
import org.orekit.propagation.events.handlers.ContinueOnEvent;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;


import java.time.Instant;
import java.time.temporal.ChronoUnit;

/* Clase encargada de calcular la posición estimada del satélite con base en las coordenadas dadas.
 * No quiere decir que el satélite tenga imágenes en el intervalo asignado. */
public class PositionCalculator {

    public static Interval timeInterval(double latitude, double longitude, double altitude){
        //Se obtiene el instante actual del tiempo
        Instant now = Instant.now();
        AbsoluteDate fiveDaysAgo = new AbsoluteDate(now.minus(5, ChronoUnit.DAYS).toString(),
                TimeScalesFactory.getUTC());

        //Se obtiene la forma de la tierra en conjunto de sus características estandarizadas de ecuador, rotación, gravedad, etc.
        Frame earthFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING, earthFrame);

        //Se define el punto al que se quiere observar
        GeodeticPoint observedPoint = new GeodeticPoint(
                FastMath.toRadians(latitude),
                FastMath.toRadians(longitude),
                altitude);

        //Se crea estación de monitoreo (Ajustar elevación mínima)
        TopocentricFrame station = new TopocentricFrame(earth, observedPoint, "Observed Point");
        double minElevation = FastMath.toRadians(10.0);

        //Configuración de detección cuando el satélite esté a 10 grados por encima del horizonte.
        ElevationDetector detector = new ElevationDetector(60.0, 0.001, station)
                .withConstantElevation(minElevation)
                .withHandler(new ContinueOnEvent());

        //Datos de la dirección del satélite que se quiere calcular la trayectoria
        String line1 = "1 39634U 14016A   26008.59612065  .00000077  00000-0  26005-4 0  9998";
        String line2 = "2 39634  98.1803  18.4404 0001280  85.2473 274.8873 14.59200465626747";

        TLE sentinelTLE = new TLE(line1, line2);

        //El objeto propagator calcula las físicas del satélite
        TLEPropagator propagator = TLEPropagator.selectExtrapolator(sentinelTLE);

        //Se añade logger para registrar eventos
        EventsLogger logger = new EventsLogger();
        propagator.addEventDetector(logger.monitorDetector(detector));

        //Se corre la simulación por 1 día desde el día de inicio
        AbsoluteDate startDate = fiveDaysAgo;
        AbsoluteDate endDate = startDate.shiftedBy(3600.0 * 24);

        System.out.println("Simulating from " + startDate + " to " + endDate + "...");
        propagator.propagate(startDate, endDate);

        /*Se accede a los registros de los eventos y se obtiene la visibilidad del satélite
          en cada órbita del intervalo del tiempo establecido*/
        EventsLogger.LoggedEvent lastRising = null;
        Interval lastValidInterval = null;

        for(EventsLogger.LoggedEvent event : logger.getLoggedEvents()) {

            if (event.isIncreasing()) {
                //Se guarda el inicio temporalmente
                lastRising = event;
                System.out.println("Start (Rising): " + event.getState().getDate());
            } else {
                //Si hay una puesta, se verifica que se tenga una elevación por encima del horizonte
                if (lastRising != null) {
                    AbsoluteDate start = lastRising.getState().getDate();
                    AbsoluteDate end = event.getState().getDate();

                    System.out.println("End (Setting): " + end);

                    //Último intervalo válido conocido
                    lastValidInterval = new Interval(start, end);

                    //Se reinicia lastRising para esperar el siguiente ciclo
                    lastRising = null;
                }
            }
        }

        //Validación final
        if (lastValidInterval != null) {

            /*Se configura una ventana de tiempo en el intervalo en caso de que el satélite necesite un rango
              mayor para la obtención de imágenes*/
            AbsoluteDate searchStart = lastValidInterval.getStartTime().shiftedBy(-0.0); // 0 min
            AbsoluteDate searchEnd = lastValidInterval.getEndTime().shiftedBy(345600.0); // +4 días

            //Retornamos el intervalo extendido
            Interval expandedInterval = new Interval(searchStart, searchEnd);

            return expandedInterval;
        } else {
            System.out.println("No hubo ningún paso completo");
            return null;
        }
    }
}