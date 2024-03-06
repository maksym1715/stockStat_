package team606.stockStat.communication.dto;

import java.util.List;

import lombok.Data;
@Data
public class PeriodDataCloseBetween {
	 private String from;
	    private String to;
	    private String source;
	    private String type;
	    private String minDate;
	    private String maxDate;
	    private Double startClose;
	    private Double endClose;
	    private Double valueClose;
	    private List<Double> listClose;

}
