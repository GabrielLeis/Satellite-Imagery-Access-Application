package org.gabrielleis;

import org.orekit.time.AbsoluteDate;

/* Clase de la entidad Interval, la cual estructura un intervalo de tiempo con un
*  inicio y un final determinado. */
public class Interval {
    private AbsoluteDate startTime;
    private AbsoluteDate endTime;

    public Interval(AbsoluteDate startTime, AbsoluteDate endTime){
        this.startTime = startTime;
        this.endTime = endTime;
    }

    //Getters
    public AbsoluteDate getStartTime() { return startTime; }
    public AbsoluteDate getEndTime() { return endTime; }
}
