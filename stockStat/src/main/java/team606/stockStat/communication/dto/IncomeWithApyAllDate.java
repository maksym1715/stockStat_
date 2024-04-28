package team606.stockStat.communication.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
public class IncomeWithApyAllDate {
	private String source;
    private LocalDate historyFrom;
    private LocalDate historyTo;
    private String type;
    private String from;
    private String to;
    private double purchaseAmount;
    private double saleAmount;
    private double income;
    private double apy;
}
