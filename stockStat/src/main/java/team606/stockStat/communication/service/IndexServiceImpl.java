package team606.stockStat.communication.service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import team606.stockStat.communication.dao.CsvDataRepository;
import team606.stockStat.communication.dao.TimePeriods;
import team606.stockStat.communication.dao.UploadInfoRepository;
import team606.stockStat.communication.dto.CalculateIncomeWithApyRequest;
import team606.stockStat.communication.dto.CalculateSumPackageRequest;
import team606.stockStat.communication.dto.CorrelationRequest;
import team606.stockStat.communication.dto.IncomeWithApy;
import team606.stockStat.communication.dto.PeriodData;
import team606.stockStat.communication.dto.PeriodRequest;
import team606.stockStat.communication.dto.ResponseDto;
import team606.stockStat.communication.parser.CsvData;
import team606.stockStat.communication.parser.UploadInfo;

import static java.util.stream.Collectors.toList;

@Service
public class IndexServiceImpl implements IndexService {

    private final CsvDataRepository csvDataRepository;
    private final UploadInfoRepository uploadInfoRepository;

    public IndexServiceImpl(CsvDataRepository csvDataRepository, UploadInfoRepository uploadInfoRepository) {
        this.csvDataRepository = csvDataRepository;
        this.uploadInfoRepository = uploadInfoRepository;
    }

    @Override
    public List<ResponseDto> getAllDataBySources(TimePeriods timePeriods, List<String> source, LocalDate from, LocalDate to, Long quantity) {
     
    	 List<UploadInfo>listUploadInfo = uploadInfoRepository.findAllBySourceInAndDateIsAfterAndDateIsBefore(source, from, to);
                
    	 Map<String, List<UploadInfo>> allBySource = listUploadInfo
    			.stream()
                .collect(Collectors.groupingBy(UploadInfo::getSource));
    	
        //System.out.println("run");
        List<ResponseDto> result = new ArrayList<>();
        for (Map.Entry<String, List<UploadInfo>> entry : allBySource.entrySet()) {
            Map<LocalDate, CsvData> allByUploadInfoIdIn = csvDataRepository.findAllByUploadInfoIdIn(entry.getValue())
                    .stream()
                    .filter(csvData ->{
                    	return csvData.getUploadInfoId() != null;
                    })
                    .collect(Collectors.toMap(a -> a.getUploadInfoId().getDate(), Function.identity(),
                            BinaryOperator.maxBy(Comparator.comparing(csvData -> csvData.getUploadInfoId().getDate()))));
            
            for (Map.Entry<LocalDate, CsvData> objectObjectEntry : allByUploadInfoIdIn.entrySet()) {
                LocalDate firstDate = objectObjectEntry.getValue().getUploadInfoId().getDate();
                LocalDate secondDate = TimePeriods.getAnalyze(timePeriods, firstDate, quantity);
               // CsvData firstData = objectObjectEntry.getValue();
              //  CsvData csvDataLastPeriod = allByUploadInfoIdIn.get(secondDate);
                List<Double> values = allByUploadInfoIdIn.values().stream()
                		.map(CsvData::getAllValues)
                        .flatMap(map -> map.values().stream())
                        .collect(Collectors.toList());
                
                List<Double> closingPrices = allByUploadInfoIdIn.values().stream()
                        .map(csvData -> csvData.getClose())
                        .collect(Collectors.toList());

                ResponseDto responseDto = new ResponseDto();
                responseDto.setFrom(firstDate);
                responseDto.setTo(secondDate);
                responseDto.setSource(entry.getKey());
                responseDto.setType(quantity + " " + timePeriods.name().toLowerCase());
                responseDto.setMax(Collections.max(closingPrices) - closingPrices.get(0));
                responseDto.setMean(values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
                responseDto.setMedian(calculateMedian(values));
                responseDto.setMin(Collections.min(closingPrices) - closingPrices.get(0));
                responseDto.setStd(calculateStandardDeviation(values));

                result.add(responseDto);
            }

            
        }
        return result;
    }

    private Double calculateMedian(List<Double> values) {
        Collections.sort(values);
        int size = values.size();
        if (size % 2 == 0) {
            return (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
        } else {
            return values.get(size / 2);
        }
    }

    private Double calculateStandardDeviation(List<Double> values) {
        Double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        Double sumOfSquaredDifferences = values.stream().mapToDouble(value -> Math.pow(value - mean, 2)).sum();
        int size = values.size();
        return Math.sqrt(sumOfSquaredDifferences / (double) size);
    }

    @Override
    public List<CsvData> getTimeHistoryForIndex(String indexName) {
        List<UploadInfo> allBySource = uploadInfoRepository.findAllBySource(indexName);
        Collections.sort(allBySource, new Comparator<UploadInfo>() {
            @Override
            public int compare(UploadInfo a1, UploadInfo a2) {
                return a1.getDate().compareTo(a2.getDate());
            }


        });
        List<UploadInfo> list = Arrays.asList(allBySource.get(0), allBySource.get(allBySource.size() - 1));
        return csvDataRepository.findAllByUploadInfoIdIn(list);
    }

    @Override
    public List<String> getAllIndexes() {

       
    	List<String> uniqueIndexes = uploadInfoRepository.findAllDistinctIndexes();
        return uniqueIndexes != null ? uniqueIndexes : Collections.emptyList();
        

    }
    
    public List<PeriodData> getAllValueCloseBetween(PeriodRequest request) {
        List<ResponseDto> responseDtos = getAllDataBySources(
                TimePeriods.valueOf(request.getType()),
                request.getIndexes(),
                request.getFrom(),
                request.getTo(),
                Long.valueOf(request.getQuantity())
        );

        return responseDtos.stream()
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
    }

	@Override
	public List<PeriodData> calculateSumPackage(CalculateSumPackageRequest request) {
		List<UploadInfo> listUploadInfo = uploadInfoRepository.findAllBySourceInAndDateIsAfterAndDateIsBefore(
	            request.getIndexs(), LocalDate.parse(request.getFrom()), LocalDate.parse(request.getTo()));

	    Map<String, List<UploadInfo>> allBySource = listUploadInfo.stream()
	            .collect(Collectors.groupingBy(UploadInfo::getSource));

	    List<PeriodData> result = new ArrayList<>();

	    for (Map.Entry<String, List<UploadInfo>> entry : allBySource.entrySet()) {
	        List<UploadInfo> uploadInfos = entry.getValue();

	        // Collecting all CsvData for the given source and time period
	        List<CsvData> csvDataList = csvDataRepository.findAllByUploadInfoIdIn(uploadInfos);
	        
	        // Creating a map with dates as keys and corresponding CsvData as values
	        Map<LocalDate, CsvData> allByUploadInfoIdIn = csvDataList.stream()
	                .filter(csvData -> csvData.getUploadInfoId() != null)
	                .collect(Collectors.toMap(a -> a.getUploadInfoId().getDate(), Function.identity(),
	                        BinaryOperator.maxBy(Comparator.comparing(csvData -> csvData.getUploadInfoId().getDate()))));

	        for (Map.Entry<LocalDate, CsvData> objectObjectEntry : allByUploadInfoIdIn.entrySet()) {
	            LocalDate firstDate = objectObjectEntry.getKey();
	            LocalDate secondDate = TimePeriods.getAnalyze(TimePeriods.valueOf(request.getType()), firstDate, Long.valueOf(request.getQuantity()));

	            List<Double> closingPrices = allByUploadInfoIdIn.values().stream()
	                    .map(csvData -> csvData.getClose())
	                    .collect(Collectors.toList());

	            // Summing up the closing prices for each stock in the package
	            double sum = IntStream.range(0, request.getIndexs().size())
	                    .mapToDouble(i -> request.getAmount().get(i) * closingPrices.get(i))
	                    .sum();

	            // Creating PeriodData with calculated sum and other statistics
	            PeriodData periodData = new PeriodData();
	            periodData.setFrom(firstDate);
	            periodData.setTo(secondDate);
	            periodData.setSource(entry.getKey());
	            periodData.setType(request.getQuantity() + " " + request.getType().toLowerCase());
	            periodData.setMean(sum);  // Assuming mean is the sum in this context
	            periodData.setMin(Collections.min(closingPrices) * request.getAmount().get(0));  // Assuming min is the min of the first stock
	            periodData.setMax(Collections.max(closingPrices) * request.getAmount().get(0));  // Assuming max is the max of the first stock
	            periodData.setStd(calculateStandardDeviation(closingPrices));  // Assuming std is the standard deviation of closing prices

	            result.add(periodData);
	        }
	    }
	    return result;
	}

	@Override
	public List<IncomeWithApy> calculateIncomeWithApy(CalculateIncomeWithApyRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IncomeWithApy> calculateIncomeWithApyAllDate(CalculateIncomeWithApyRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	 @Override
    public String calculateCorrelation(CorrelationRequest correlationRequest) {
        // Ваша логика расчета корреляции
        // ...

        // Возможные варианты ответа
        
            return "very strong positive correlation";
        
    }

	

    
    


}
