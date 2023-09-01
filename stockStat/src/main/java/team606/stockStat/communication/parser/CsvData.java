package team606.stockStat.communication.parser;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CsvData {
	
	private UploadInfo uploadInfoId;
    private double close;
    private double volume;
    private double open;
    private double high;
    private double low;
        
}
