package team606.stockStat.communication.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IncomeWithIrr {
    private String from;
    private String to;
    private String type;
    private List<String> source;
    private SubPeriodWithIrr minIncome;
    private SubPeriodWithIrr maxIncome;
}

