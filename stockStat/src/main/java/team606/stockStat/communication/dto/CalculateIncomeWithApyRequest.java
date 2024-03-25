package team606.stockStat.communication.dto;

import java.util.List;

import lombok.Data;

@Data
public class CalculateIncomeWithApyRequest {
	private List<String> indexs;
    private String type;
    private Long quantity;
    private String from;
    private String to;
}
