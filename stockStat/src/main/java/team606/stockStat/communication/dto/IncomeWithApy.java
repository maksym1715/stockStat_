package team606.stockStat.communication.dto;

import lombok.Data;

@Data
public class IncomeWithApy {
	private String source;
    private String historyFrom;
    private String historyTo;
    private String type;
    private String from;
    private String to;
    private double purchaseAmount;
    private double saleAmount;
    private double income;
    private double apy;
}
