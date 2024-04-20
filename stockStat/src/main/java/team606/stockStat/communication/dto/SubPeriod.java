package team606.stockStat.communication.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SubPeriod {
    private String source;
    private String historyFrom;
    private String historyTo;
    private String type;
    private String from;
    private String to;
    private LocalDate dateOfPurchase;
    private double purchaseAmount;
    private LocalDate dateOfSale;
    private double saleAmount;
    private double income;
    private double irr;
    private double apy;
}
