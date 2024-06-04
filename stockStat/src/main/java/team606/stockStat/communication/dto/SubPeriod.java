package team606.stockStat.communication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubPeriod {
	/*
	 * private String source; private String historyFrom; private String historyTo;
	 * private String type; private String from; private String to; private
	 * LocalDate dateOfPurchase; private double purchaseAmount; private LocalDate
	 * dateOfSale; private double saleAmount; private double income; private double
	 * irr; private double apy;
	 */
	
	private String from;
    private String to;
    private String type;
    private double purchaseAmount;
    private double saleAmount;
    private double income;
    private double apy;
	
}
