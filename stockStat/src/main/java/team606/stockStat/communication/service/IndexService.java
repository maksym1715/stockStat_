package team606.stockStat.communication.service;

import java.util.List;

import team606.stockStat.communication.parser.CsvData;


public interface IndexService {
	
	List<CsvData> getTimeHistoryForIndex(String indexName);
	
	List<String> getAllIndexes();
	
	
	

}
