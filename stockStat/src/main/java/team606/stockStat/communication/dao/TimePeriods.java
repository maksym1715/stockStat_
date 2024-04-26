package team606.stockStat.communication.dao;

import java.time.LocalDate;

public enum TimePeriods {
	 DAYS, WEEKS, MONTHS, DECADES, YEARS, CENTURIES;

    public static LocalDate getAnalyze(TimePeriods value, LocalDate to, Long quantity) {
        switch (value) {
            case DAYS:
                return to.plusDays(quantity);
            case WEEKS:
                return to.plusWeeks(quantity);
            case MONTHS:
                return to.plusMonths(quantity);
            case DECADES:
                return to.plusYears(10 * quantity);
            case YEARS:
                return to.plusYears(quantity);
            case CENTURIES:
                return to.plusYears(100);
        }

        return to;
    }
}
