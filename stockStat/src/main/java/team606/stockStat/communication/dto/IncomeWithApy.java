package team606.stockStat.communication.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class IncomeWithApy {
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
