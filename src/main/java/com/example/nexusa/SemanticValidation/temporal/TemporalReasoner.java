package com.example.nexusa.SemanticValidation.temporal;

import org.springframework.stereotype.Component;
import com.example.nexusa.Model.GlobalModels.HistoricalInterval;

@Component
public class TemporalReasoner {

    public boolean overlaps(Long aStart, Long aEnd, Long bStart, Long bEnd) {
        return new HistoricalInterval(aStart, aEnd).overlaps(new HistoricalInterval(bStart, bEnd));
    }

    public boolean isInverted(Long startYear, Long endYear) {
        return new HistoricalInterval(startYear, endYear).isInverted();
    }

    public Long ageAt(Long birthYear, Long eventYear) {
        if (birthYear == null || eventYear == null) {
            return null;
        }
        return eventYear - birthYear;
    }

    public double precisionFactor(Long startYear, Long endYear) {
        if (startYear != null && endYear != null) {
            return 1.0;
        }
        if (startYear != null || endYear != null) {
            return 0.72;
        }
        return 0.45;
    }
}
