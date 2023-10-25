package team606.stockStat.communication.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter

@Setter
public class PeriodRequest {
	 private List<String> indexes;
	    private String type;
	    private int quantity;
	    private LocalDate from;
	    private LocalDate to;
}
