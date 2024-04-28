package team606.stockStat.communication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubPeriodWithIrr {
    private String index;
    private String dateOfPurchase;
    private double purchaseAmount;
    private String dateOfSale;
    private double saleAmount;
    private double income;
    private double irr;
}
