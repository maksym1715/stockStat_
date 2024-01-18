package team606.stockStat.communication.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter

@Setter
public class ResponseDto {
	private LocalDate from;
    private LocalDate to;
    private String source;
    private String type;
    private Double max;
    private Double mean;
    private Double median;
    private Double min;
    private Double std;
    
}
