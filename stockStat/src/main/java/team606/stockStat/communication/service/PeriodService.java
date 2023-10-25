package team606.stockStat.communication.service;

import java.util.List;

import team606.stockStat.communication.dto.PeriodData;
import team606.stockStat.communication.dto.PeriodRequest;

public interface PeriodService {
	
	List<PeriodData> getPeriodBetween(PeriodRequest request);

}
