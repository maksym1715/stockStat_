package team606.stockStat.communication.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncomeWithApy {
	private LocalDate dateOfPurchase;
    private double purchaseAmount;
    private LocalDate dateOfSale;
    private double saleAmount;
    private double income;
    private double apy;
}
