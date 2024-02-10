package team606.stockStat.communication.dto;

import java.util.List;

import lombok.Data;

@Data
public class CalculateSumPackageRequest {
	private List<String> indexs;
    private List<Integer> amount;
    private String from;
    private String to;
    private String type;
    private int quantity;
}
