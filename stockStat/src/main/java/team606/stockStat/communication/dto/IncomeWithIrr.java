package team606.stockStat.communication.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class IncomeWithIrr {
    private LocalDate from;
    private LocalDate to;
    private String source;
    private String type;
    private SubPeriod minIncome;
    private SubPeriod maxIncome;
}

