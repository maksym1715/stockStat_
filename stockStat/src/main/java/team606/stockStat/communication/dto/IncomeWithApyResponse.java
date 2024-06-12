package team606.stockStat.communication.dto;

import java.util.List;

import lombok.Data;
@Data
public class IncomeWithApyResponse {
	private String from;
    private String to;
    private List<String> source;
    private String type;
    private IncomeWithApy minIncome;
    private IncomeWithApy maxIncome;
}
