package team606.stockStat.communication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IRRHistory {
	private String date;
    private double irr;
}
