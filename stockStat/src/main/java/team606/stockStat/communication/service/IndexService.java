package team606.stockStat.communication.service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

import team606.stockStat.communication.dao.TimePeriods;
import team606.stockStat.communication.dto.CalculateIncomeWithApyRequest;
import team606.stockStat.communication.dto.CalculateSumPackageRequest;
import team606.stockStat.communication.dto.CorrelationRequest;
import team606.stockStat.communication.dto.IncomeWithApy;
import team606.stockStat.communication.dto.IncomeWithApyAllDate;
import team606.stockStat.communication.dto.IncomeWithApyResponse;
import team606.stockStat.communication.dto.IncomeWithIrr;
import team606.stockStat.communication.dto.PeriodData;
import team606.stockStat.communication.dto.PeriodDataCloseBetween;
import team606.stockStat.communication.dto.PeriodRequest;
import team606.stockStat.communication.dto.ResponseDto;
import team606.stockStat.communication.dto.TimeHistoryData;
import team606.stockStat.communication.parser.CsvData;
import team606.stockStat.communication.parser.SourceData;

public interface IndexService {

	TimeHistoryData getTimeHistoryForIndex(String indexName);

	List<String> getAllIndexes();

	List<ResponseDto> getAllDataBySources(TimePeriods timePeriods, List<String> source, LocalDate from, LocalDate to,
			Long quantity);

	List<PeriodDataCloseBetween> getAllValueCloseBetween(TimePeriods timePeriods, List<String> source, LocalDate from, LocalDate to,
			Long quantity);

	

	IncomeWithApyResponse calculateIncomeWithApy(CalculateIncomeWithApyRequest request);
    //TODO we get empty result

	List<IncomeWithApyAllDate> calculateIncomeWithApyAllDate(CalculateIncomeWithApyRequest request);
    //TODO we get empty result

	List<IncomeWithIrr> calculateIncomeWithIrr(CalculateIncomeWithApyRequest request);
    //TODO not done

	String calculateCorrelation(CorrelationRequest correlationRequest);

	Boolean deleteAllHistoryForCompany(String indexName);

	List<ResponseDto> calculateSumPackage(List<String> indexes, List<Integer> amounts, LocalDate from, LocalDate to,
			TimePeriods timePeriods, Long quantity);

	
	

	   

}
