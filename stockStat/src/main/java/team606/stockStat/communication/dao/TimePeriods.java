package team606.stockStat.communication.dao;

import java.time.LocalDate;

public enum TimePeriods {
    days, weeks, months, decades, years, centuries;

    public static LocalDate getAnalyze(TimePeriods value, LocalDate to, Long quantity) {
        switch (value) {
            case days:
                return to.plusDays(quantity);
            case weeks:
                return to.plusWeeks(quantity);
            case months:
                return to.plusMonths(quantity);
            case decades:return to.plusDays(10*quantity);
            case years:
                return to.plusYears(quantity);
            case centuries:
                return to.plusYears(100);
        }

        return to;
    }
}
