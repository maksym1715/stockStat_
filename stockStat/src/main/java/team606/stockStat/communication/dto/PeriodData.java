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
public class PeriodData {
	
	private LocalDate from;
    private LocalDate to;
    private String source;
    private String type;
    private double max;
    private double mean;
    private double median;
    private double min;
    private double std;

}
