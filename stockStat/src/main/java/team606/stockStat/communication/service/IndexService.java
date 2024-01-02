package team606.stockStat.communication.service;

import java.time.LocalDate;
import java.util.List;

import team606.stockStat.communication.dao.TimePeriods;
import team606.stockStat.communication.parser.CsvData;


public interface IndexService {
	
	List<CsvData> getTimeHistoryForIndex(String indexName);
	
	List<String> getAllIndexes();
	List<CsvData> getAllDataBySources(TimePeriods timePeriods, List<String> source, LocalDate from, LocalDate to, Long quantity);
	
	
	

}
