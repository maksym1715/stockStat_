package team606.stockStat.communication.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.exceptions.CsvException;

import lombok.RequiredArgsConstructor;
import team606.stockStat.communication.dao.TimePeriods;
import team606.stockStat.communication.dto.CalculateIncomeWithApyRequest;
import team606.stockStat.communication.dto.CalculateSumPackageRequest;
import team606.stockStat.communication.dto.CorrelationRequest;
import team606.stockStat.communication.dto.IncomeWithApy;
import team606.stockStat.communication.dto.PeriodData;
import team606.stockStat.communication.dto.PeriodRequest;
import team606.stockStat.communication.dto.ResponseDto;
import team606.stockStat.communication.dto.TimeHistoryData;
import team606.stockStat.communication.parser.CsvData;
import team606.stockStat.communication.parser.CsvParser;
import team606.stockStat.communication.parser.SourceData;
import team606.stockStat.communication.service.IndexService;
import team606.stockStat.communication.service.PeriodService;

@RestController
@RequestMapping("/communication")
@RequiredArgsConstructor
public class CommunicetionController {
	
	private final IndexService indexService;
	private final PeriodService periodService;
	
	 

	@Autowired
	private CsvParser csvParser;
	
	@PostMapping("/parser")
	public List<CsvData> parseCsv(@RequestParam MultipartFile file, @RequestParam String fromDate,
			@RequestParam String toDate) throws IOException, CsvException, java.text.ParseException {
		return csvParser.parseCsvFile(file, fromDate, toDate);
	}
	@GetMapping("/index/{indexName}")
    public ResponseEntity<TimeHistoryData> getTimeHistoryForIndex(@PathVariable String indexName) {
		TimeHistoryData response = indexService.getTimeHistoryForIndex(indexName);
        return ResponseEntity.ok(response);
    }
    
	 @GetMapping("/index")
	    public ResponseEntity<List<String>> getAllIndexes() {
	        List<String> indexes = indexService.getAllIndexes();
	        return ResponseEntity.ok(indexes);
	    }
    
    @PostMapping("/period")
    public ResponseEntity<List<PeriodData>> getPeriodBetween(@RequestBody PeriodRequest request) {
        List<ResponseDto> responseDtos = indexService.getAllDataBySources(
        		TimePeriods.valueOf(request.getType()),
                request.getIndexes(),
                request.getFrom(),
                request.getTo(),
                Long.valueOf(request.getQuantity())
        );

        List<PeriodData> periodData = responseDtos.stream()
                .map(responseDto -> {
                    PeriodData periodDataItem = new PeriodData();
                    periodDataItem.setFrom(responseDto.getFrom());
                    periodDataItem.setTo(responseDto.getTo());
                    periodDataItem.setSource(responseDto.getSource());
                    periodDataItem.setType(responseDto.getType());
                    periodDataItem.setMax(responseDto.getMax());
                    periodDataItem.setMean(responseDto.getMean());
                    periodDataItem.setMedian(responseDto.getMedian());
                    periodDataItem.setMin(responseDto.getMin());
                    periodDataItem.setStd(responseDto.getStd());
                    return periodDataItem;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(periodData);
    }
    
    @PostMapping("/data")
    public ResponseEntity<List<PeriodData>> getAllValueCloseBetween(@RequestBody PeriodRequest request) {
        List<PeriodData> periodData = indexService.getAllValueCloseBetween(request);
        return ResponseEntity.ok(periodData);
    }
    
    @PostMapping("/index/sum")
    public ResponseEntity<PeriodData> calculateSumPackage(@RequestBody CalculateSumPackageRequest request) {
        List<ResponseDto> responseDtos = indexService.calculateSumPackage(
                request.getIndexs(),
                request.getAmount(),
                LocalDate.parse(request.getFrom()),
                LocalDate.parse(request.getTo()),
                TimePeriods.valueOf(request.getType()),
                Long.valueOf(request.getQuantity())
        );

        // Получаем последний элемент из списка
        ResponseDto lastResult = responseDtos.get(responseDtos.size() - 1);

        PeriodData periodData = new PeriodData();
        periodData.setFrom(lastResult.getFrom());
        periodData.setTo(lastResult.getTo());
        periodData.setSource("Package for: " + String.join(", ", request.getIndexs()));
        periodData.setType(lastResult.getType());
        periodData.setMax(lastResult.getMax());
        periodData.setMean(lastResult.getMean());
        periodData.setMedian(lastResult.getMedian());
        periodData.setMin(lastResult.getMin());
        periodData.setStd(lastResult.getStd());

        return ResponseEntity.ok(periodData);
    }

    @PostMapping("/index/apy")
    public ResponseEntity<List<IncomeWithApy>> calculateIncomeWithApy(@RequestBody CalculateIncomeWithApyRequest request) {
        List<IncomeWithApy> result = indexService.calculateIncomeWithApy(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/index/apy_all")
    public ResponseEntity<List<IncomeWithApy>> calculateIncomeWithApyAllDate(@RequestBody CalculateIncomeWithApyRequest request) {
        List<IncomeWithApy> result = indexService.calculateIncomeWithApyAllDate(request);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/index/correlation")
    public String calculateCorrelation(@RequestBody CorrelationRequest correlationRequest) {
        return indexService.calculateCorrelation(correlationRequest);
    }
    
    @DeleteMapping("/index/{indexName}")
    public ResponseEntity<Boolean> deleteAllHistoryForCompany(@PathVariable String indexName) {
        Boolean response = indexService.deleteAllHistoryForCompany(indexName);
        return ResponseEntity.ok(response);
    }

   

}
