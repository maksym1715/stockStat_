package team606.stockStat.communication.service;

import java.util.List;

import org.springframework.stereotype.Service;


public interface IndexService {
	
	String getTimeHistoryForIndex(String indexName);
	
	List<String> getAllIndexes();
	
	
	

}
