package team606.stockStat.communication.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team606.stockStat.communication.parser.UploadInfo;

@Getter

@Setter
public class PeriodRequest {
	 private List<String> indexes;
	    private String type;
	    private Long quantity;
	    private LocalDate from;
	    private LocalDate to;
}
