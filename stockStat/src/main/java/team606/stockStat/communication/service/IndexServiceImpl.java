package team606.stockStat.communication.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import com.sun.xml.bind.v2.TODO;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import team606.stockStat.communication.dao.CsvDataRepository;
import team606.stockStat.communication.dao.TimePeriods;
import team606.stockStat.communication.dao.UploadInfoRepository;
import team606.stockStat.communication.dto.*;
import team606.stockStat.communication.parser.CsvData;
import team606.stockStat.communication.parser.SourceData;
import team606.stockStat.communication.parser.UploadInfo;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;


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
    public List<ResponseDto> getAllDataBySources(TimePeriods timePeriods, List<String> source, LocalDate from,
                                                 LocalDate to, Long quantity) {
        to = to.plusDays(1);
        List<UploadInfo> listUploadInfo = uploadInfoRepository.findAllBySourceInAndDateIsAfterAndDateIsBefore(source,
                from, to);
        Comparator<UploadInfo> comparator = Comparator.comparing(UploadInfo::getDate);

        listUploadInfo.sort(comparator);
        Map<String, List<UploadInfo>> allBySource = listUploadInfo.stream()
                .collect(Collectors.groupingBy(UploadInfo::getSource));

        // System.out.println("run");
        List<ResponseDto> result = new ArrayList<>();

        for (Map.Entry<String, List<UploadInfo>> entry : allBySource.entrySet()) {

            Map<LocalDate, CsvData> allByUploadInfoIdIn = csvDataRepository.findAllByUploadInfoIdIn(entry.getValue())

                    .stream().filter(csvData -> {
                        return csvData.getUploadInfoId() != null;
                    }).collect(Collectors.toMap(a -> a.getUploadInfoId().getDate(), Function.identity(), BinaryOperator
                            .maxBy(Comparator.comparing(csvData -> csvData.getUploadInfoId().getDate()))));

            TreeMap<LocalDate, CsvData> newMap = new TreeMap<LocalDate, CsvData>(allByUploadInfoIdIn);
            List<Double> resultList = new LinkedList<>();
            for (Map.Entry<LocalDate, CsvData> objectObjectEntry : newMap.entrySet()) {
                LocalDate firstDate = objectObjectEntry.getValue().getUploadInfoId().getDate();
                LocalDate secondDate = TimePeriods.getAnalyze(timePeriods, firstDate, quantity);
                CsvData dataFirstDate = objectObjectEntry.getValue();
                CsvData dataSecondDate = newMap.get(secondDate);
                if (dataSecondDate == null) {
                    break;
                }

                double max = dataSecondDate.getHigh() - dataFirstDate.getLow();
                double min = dataSecondDate.getLow() - dataFirstDate.getHigh();
                resultList.add(max);
                resultList.add(min);

            }
            ResponseDto responseDto = new ResponseDto();
            responseDto.setFrom(from);
            responseDto.setTo(to);
            responseDto.setSource(entry.getKey());
            responseDto.setType(quantity + " " + timePeriods.toString());
            responseDto.setMedian(calculateMedian(resultList));
            double mean = calculateMean(resultList);
            responseDto.setMean(mean);
            responseDto.setStd(calculateStandardDeviation(resultList, mean));
            responseDto.setMedian(calculateMedian(resultList));
            responseDto.setMax(findMax(resultList));
            responseDto.setMin(findMin(resultList));
            result.add(responseDto);
        }
        return result;
    }

    public static double findMin(List<Double> numbers) {
        if (numbers.isEmpty()) {
            return 0.0;
        }
        // Initialize min with the first element of the array
        double min = numbers.get(0);
        // Iterate through the array
        for (int i = 1; i < numbers.size(); i++) {
            if (numbers.get(i) < min) {
                min = numbers.get(i);
            }
        }
        return min;
    }

    public static double findMax(List<Double> numbers) {
        // Initialize max with the first element of the array
        if (numbers.isEmpty()) {
            return 0.0;
        }
        double max = numbers.get(0);
        // Iterate through the array
        for (int i = 1; i < numbers.size(); i++) {
            if (numbers.get(i) > max) {
                max = numbers.get(i);
            }
        }
        return max;
    }

    public static double calculateMean(List<Double> numbers) {
        if (numbers.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (Double num : numbers) {
            sum += num;
        }
        return sum / numbers.size();
    }

    private Double calculateMedian(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        }

        Collections.sort(values);
        int size = values.size();
        if (size % 2 == 0) {
            return (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
        } else {
            return values.get(size / 2);
        }
    }

    public static double calculateStandardDeviation(List<Double> numbers, double mean) {
        if (numbers.isEmpty() || numbers.size() == 1) {
            return 0.0;
        }

        double sum = 0.0;
        for (Double num : numbers) {
            sum += Math.pow(num - mean, 2);
        }
        double meanOfDiffs = sum / numbers.size();
        return Math.sqrt(meanOfDiffs);
    }

    public TimeHistoryData getTimeHistoryForIndex(String indexName) {
        List<UploadInfo> allBySource = uploadInfoRepository.findAllBySource(indexName);

        Collections.sort(allBySource, Comparator.comparing(UploadInfo::getDate));

        UploadInfo firstUploadInfo = allBySource.get(0);
        UploadInfo lastUploadInfo = allBySource.get(allBySource.size() - 1);

        TimeHistoryData timeHistoryData = new TimeHistoryData();
        timeHistoryData.setSource(indexName);
        timeHistoryData.setFromData(firstUploadInfo.getDate().toString());
        timeHistoryData.setToData(lastUploadInfo.getDate().toString());

        return timeHistoryData;
    }

    @Override
    public List<String> getAllIndexes() {

        List<String> uniqueIndexes = uploadInfoRepository.findAllDistinctIndexes();
        return uniqueIndexes != null ? uniqueIndexes : Collections.emptyList();

    }


    public List<SubPeriod> calculateEachDayDuringTimePeriod(TimePeriods timePeriods, List<String> source,
                                                 LocalDate from, LocalDate to, Long quantity) {
        to = to.plusDays(1);
        List<UploadInfo> listUploadInfo = uploadInfoRepository.findAllBySourceInAndDateIsAfterAndDateIsBefore(source,
                from, to);
        Comparator<UploadInfo> comparator = Comparator.comparing(UploadInfo::getDate);

        listUploadInfo.sort(comparator);
        Map<String, List<UploadInfo>> allBySource = listUploadInfo.stream()
                .collect(Collectors.groupingBy(UploadInfo::getSource));
        List<SubPeriod> result = new LinkedList<>();
        for (Map.Entry<String, List<UploadInfo>> stringListEntry : allBySource.entrySet()) {
            List<CsvData> dataList = csvDataRepository.findAllByUploadInfoIdIn(stringListEntry.getValue());
            Map<LocalDate, CsvData> localDateCsvDataMap = dataList.stream().collect(Collectors.toMap(a -> a.getUploadInfoId().getDate(), a -> a, (c, v) -> v));
            for (CsvData csvData : dataList) {
                LocalDate minDate = csvData.getUploadInfoId().getDate();
                LocalDate secondDate = TimePeriods.getAnalyze(timePeriods, minDate, quantity);
                CsvData income = localDateCsvDataMap.get(secondDate);
                SubPeriod responseDto = new SubPeriod();
                responseDto.setPurchaseAmount(income.getLow());
                responseDto.setSaleAmount(income.getHigh());
                responseDto.setIncome(income.getHigh() - income.getLow());
                responseDto.setType(timePeriods.toString() + " " + quantity.toString());
                responseDto.setFrom(minDate.toString());
                responseDto.setTo(secondDate.toString());
                responseDto.setApy(calculateApy(income.getLow(),income.getHigh(),quantity,timePeriods));
                result.add(responseDto);
            }
        }
        return result;
    }

    public static double calculateApy(double purchaseAmount, double saleAmount, long quantity, TimePeriods timePeriods) {
    	
    	double period = quantity;
        if (timePeriods.equals(TimePeriods.MONTHS)) {
        	period = period / 12;
        }
        double netGain = saleAmount - purchaseAmount;
        double annualizedReturn = netGain / purchaseAmount; // This is the total return
        return Math.pow(1 + annualizedReturn, 1 / period) - 1;

    }

    //TODO for calculation min income we need to change raw 227 and to add one more condition more than 0. For response you need to return date of value(you can get them by getting bestMinIndex and bestMaxIndex)
    public void calculate(TimePeriods timePeriods, List<String> source,
                          LocalDate from, LocalDate to, Long quantity) {

        to = to.plusDays(1);
        List<UploadInfo> listUploadInfo = uploadInfoRepository.findAllBySourceInAndDateIsAfterAndDateIsBefore(source,
                from, to);
        Comparator<UploadInfo> comparator = Comparator.comparing(UploadInfo::getDate);

        listUploadInfo.sort(comparator);
        Map<String, List<UploadInfo>> allBySource = listUploadInfo.stream()
                .collect(Collectors.groupingBy(UploadInfo::getSource));

        for (Map.Entry<String, List<UploadInfo>> entry : allBySource.entrySet()) {
            List<CsvData> dataList = csvDataRepository.findAllByUploadInfoIdIn(entry.getValue());
            for (int i = 0; i < dataList.size(); i++) {
                for (int j = i + 1; j < dataList.size(); j++) {
                    CsvData minData = dataList.get(i);
                    CsvData maxData = dataList.get(j);

                    int bestMinIndex = -1;
                    int bestMaxIndex = -1;
                    double maxDifference = 0;
                    // Ensure maxData date is after minData date
                    if (maxData.getUploadInfoId().getDate().isAfter(minData.getUploadInfoId().getDate())) {
                        double currentDifference = maxData.getHigh() - minData.getLow();
                        if (currentDifference> 0 && currentDifference > maxDifference) {
                            maxDifference = currentDifference;
                            bestMinIndex = i;
                            bestMaxIndex = j;
                        }
                    }
                }
            }
        }


    }


    @Override
    public List<PeriodDataCloseBetween> getAllValueCloseBetween(TimePeriods timePeriods, List<String> source,
                                                                LocalDate from, LocalDate to, Long quantity) {
        to = to.plusDays(1);
        List<UploadInfo> listUploadInfo = uploadInfoRepository.findAllBySourceInAndDateIsAfterAndDateIsBefore(source,
                from, to);
        Comparator<UploadInfo> comparator = Comparator.comparing(UploadInfo::getDate);

        listUploadInfo.sort(comparator);
        Map<String, List<UploadInfo>> allBySource = listUploadInfo.stream()
                .collect(Collectors.groupingBy(UploadInfo::getSource));

        List<PeriodDataCloseBetween> result = new ArrayList<>();

        for (Map.Entry<String, List<UploadInfo>> entry : allBySource.entrySet()) {
            Map<LocalDate, CsvData> allByUploadInfoIdIn = csvDataRepository.findAllByUploadInfoIdIn(entry.getValue())
                    .stream().filter(csvData -> csvData.getUploadInfoId() != null)
                    .collect(Collectors.toMap(a -> a.getUploadInfoId().getDate(), Function.identity(), BinaryOperator
                            .maxBy(Comparator.comparing(csvData -> csvData.getUploadInfoId().getDate()))));

            TreeMap<LocalDate, CsvData> newMap = new TreeMap<>(allByUploadInfoIdIn);
            List<Double> resultList = new LinkedList<>();

            for (Map.Entry<LocalDate, CsvData> objectObjectEntry : newMap.entrySet()) {
                LocalDate minDate = objectObjectEntry.getValue().getUploadInfoId().getDate();
                LocalDate maxDate = TimePeriods.getAnalyze(timePeriods, minDate, quantity);

                CsvData dataMinDate = objectObjectEntry.getValue();
                CsvData dataMaxDate = newMap.get(maxDate);

                if (dataMaxDate == null) {
                    break;
                }

                double startClose = dataMinDate.getLow();
                double endClose = dataMaxDate.getHigh();
                double valueClose = endClose - startClose;
                resultList.add(valueClose);

                List<Double> listClose = new ArrayList<>();
                newMap.subMap(minDate, true, maxDate, true)
                        .forEach((date, csvData) -> listClose.add(csvData.getClose()));

                PeriodDataCloseBetween periodDataCloseBetween = new PeriodDataCloseBetween();
                periodDataCloseBetween.setFrom(minDate.toString());
                periodDataCloseBetween.setTo(maxDate.toString());
                periodDataCloseBetween.setSource(entry.getKey());
                periodDataCloseBetween.setType(quantity + " " + timePeriods.toString());
                periodDataCloseBetween.setMinDate(minDate.toString());
                periodDataCloseBetween.setMaxDate(maxDate.toString());
                periodDataCloseBetween.setStartClose(startClose);
                periodDataCloseBetween.setEndClose(endClose);
                periodDataCloseBetween.setValueClose(valueClose);
                periodDataCloseBetween.setListClose(listClose);

                result.add(periodDataCloseBetween);
            }
        }
        return result;
    }

    @Override
    public List<ResponseDto> calculateSumPackage(List<String> indexes, List<Integer> amounts, LocalDate from,
                                                 LocalDate to, TimePeriods timePeriods, Long quantity) {

        to = to.plusDays(1);
        List<UploadInfo> listUploadInfo = uploadInfoRepository.findAllBySourceInAndDateIsAfterAndDateIsBefore(indexes,
                from, to);
        Comparator<UploadInfo> comparator = Comparator.comparing(UploadInfo::getDate);

        listUploadInfo.sort(comparator);
        Map<String, List<UploadInfo>> allBySource = listUploadInfo.stream()
                .collect(Collectors.groupingBy(UploadInfo::getSource));

        List<ResponseDto> result = new ArrayList<>();

        for (Map.Entry<String, List<UploadInfo>> entry : allBySource.entrySet()) {
            String source = entry.getKey();
            int index = indexes.indexOf(source);
            Map<LocalDate, CsvData> allByUploadInfoIdIn = csvDataRepository.findAllByUploadInfoIdIn(entry.getValue())
                    .stream().filter(csvData -> csvData.getUploadInfoId() != null)
                    .collect(Collectors.toMap(a -> a.getUploadInfoId().getDate(), Function.identity(), BinaryOperator
                            .maxBy(Comparator.comparing(csvData -> csvData.getUploadInfoId().getDate()))));

            TreeMap<LocalDate, CsvData> newMap = new TreeMap<>(allByUploadInfoIdIn);
            List<Double> resultList = new LinkedList<>();
            for (Map.Entry<LocalDate, CsvData> objectObjectEntry : newMap.entrySet()) {
                LocalDate firstDate = objectObjectEntry.getValue().getUploadInfoId().getDate();
                LocalDate secondDate = TimePeriods.getAnalyze(timePeriods, firstDate, quantity);
                CsvData dataFirstDate = objectObjectEntry.getValue();
                CsvData dataSecondDate = newMap.get(secondDate);
                if (dataSecondDate == null) {
                    break;
                }

                double max = (dataSecondDate.getHigh() * amounts.get(index))
                        - (dataFirstDate.getLow() * amounts.get(index));
                double min = (dataSecondDate.getLow() * amounts.get(index))
                        - (dataFirstDate.getHigh() * amounts.get(index));

                resultList.add(max);
                resultList.add(min);
            }
            ResponseDto responseDto = new ResponseDto();
            responseDto.setFrom(from);
            responseDto.setTo(to);
            responseDto.setSource(entry.getKey());
            responseDto.setType(quantity + " " + timePeriods.toString());
            responseDto.setMedian(calculateMedian(resultList));
            double mean = calculateMean(resultList);
            responseDto.setMean(mean);
            responseDto.setStd(calculateStandardDeviation(resultList, mean));
            responseDto.setMax(findMax(resultList));
            responseDto.setMin(findMin(resultList));
            result.add(responseDto);
        }

        ResponseDto totalSum = calculateTotalSum(result);
        result.add(totalSum);

        return result;
    }

    private ResponseDto calculateTotalSum(List<ResponseDto> result) {
        ResponseDto totalSum = new ResponseDto();
        List<Double> maxList = new ArrayList<>();
        List<Double> meanList = new ArrayList<>();
        List<Double> medianList = new ArrayList<>();
        List<Double> minList = new ArrayList<>();
        List<Double> stdList = new ArrayList<>();

        for (ResponseDto dto : result) {
            maxList.add(dto.getMax());
            meanList.add(dto.getMean());
            medianList.add(dto.getMedian());
            minList.add(dto.getMin());
            stdList.add(dto.getStd());
        }

        totalSum.setFrom(result.get(0).getFrom());
        totalSum.setTo(result.get(0).getTo());
        totalSum.setSource(
                "Package for: " + result.stream().map(ResponseDto::getSource).collect(Collectors.joining(", ")));
        totalSum.setType(result.get(0).getType());
        totalSum.setMax(maxList.stream().mapToDouble(Double::doubleValue).sum());
        totalSum.setMean(meanList.stream().mapToDouble(Double::doubleValue).sum());
        totalSum.setMedian(medianList.stream().mapToDouble(Double::doubleValue).sum());
        totalSum.setMax(maxList.stream().mapToDouble(Double::doubleValue).sum());
        totalSum.setMin(minList.stream().mapToDouble(Double::doubleValue).sum());
        totalSum.setStd(stdList.stream().filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum());

        return totalSum;
    }

    @Override
    public List<IncomeWithApy> calculateIncomeWithApy(CalculateIncomeWithApyRequest request) {
        // Check if the request and its parameters are present
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }
        if (request.getIndexs() == null || request.getType() == null || request.getFrom() == null || request.getTo() == null || request.getQuantity() == null) {
            throw new IllegalArgumentException("Request parameters must not be null");
        }

     
        LocalDate fromDate = LocalDate.parse(request.getFrom());
        LocalDate toDate = LocalDate.parse(request.getTo()).plusDays(1);
        Long quantity = request.getQuantity();
        TimePeriods timePeriodType = TimePeriods.valueOf(request.getType().toUpperCase());

        List<UploadInfo> uploadInfos = uploadInfoRepository.findAllBySourceInAndDateIsAfterAndDateIsBefore(
            request.getIndexs(), fromDate, toDate
        );

        Map<String, List<UploadInfo>> dataBySource = uploadInfos.stream()
            .sorted(Comparator.comparing(UploadInfo::getDate))
            .collect(Collectors.groupingBy(UploadInfo::getSource));

        List<IncomeWithApy> result = new ArrayList<>();

        for (Map.Entry<String, List<UploadInfo>> entry : dataBySource.entrySet()) {
            List<CsvData> dataList = csvDataRepository.findAllByUploadInfoIdIn(entry.getValue());

            double maxDifference = 0;
            int bestMinIndex = -1;
            int bestMaxIndex = -1;

            for (int i = 0; i < dataList.size(); i++) {
                CsvData minData = dataList.get(i);
                LocalDate minDate = minData.getUploadInfoId().getDate();
                LocalDate analyzeDate = TimePeriods.getAnalyze(timePeriodType, minDate, quantity);

                for (int j = i + 1; j < dataList.size(); j++) {
                    CsvData maxData = dataList.get(j);

                    if (maxData.getUploadInfoId().getDate().isAfter(minDate) && maxData.getUploadInfoId().getDate().isBefore(analyzeDate)) {
                        double currentDifference = maxData.getHigh() - minData.getLow();
                        if (currentDifference > 0 && currentDifference > maxDifference) {
                            maxDifference = currentDifference;
                            bestMinIndex = i;
                            bestMaxIndex = j;
                        }
                    }
                }
            }
            
            

            if (bestMinIndex >= 0 && bestMaxIndex >= 0) {
                CsvData minCsvData = dataList.get(bestMinIndex);
                CsvData maxCsvData = dataList.get(bestMaxIndex);
                
                LocalDate analyzeDate = TimePeriods.getAnalyze(timePeriodType, minCsvData.getUploadInfoId().getDate(), request.getQuantity());

                IncomeWithApy minIncome = createIncomeWithApyFromCsvData(minCsvData, analyzeDate);
                IncomeWithApy maxIncome = createIncomeWithApyFromCsvData(maxCsvData, analyzeDate);

                result.add(minIncome);
                result.add(maxIncome);
            }
        }

        return result;
    }

    private IncomeWithApy createIncomeWithApyFromCsvData(CsvData csvData, LocalDate dateOfSale) {
        IncomeWithApy incomeWithApy = new IncomeWithApy();
        incomeWithApy.setDateOfPurchase(csvData.getUploadInfoId().getDate());
        incomeWithApy.setPurchaseAmount(csvData.getLow());
        incomeWithApy.setDateOfSale(dateOfSale);
        incomeWithApy.setSaleAmount(csvData.getHigh());
        incomeWithApy.setIncome(incomeWithApy.getSaleAmount() - incomeWithApy.getPurchaseAmount());
        incomeWithApy.setApy(calculateApy(incomeWithApy.getPurchaseAmount(), incomeWithApy.getSaleAmount(), 1, TimePeriods.YEARS));
        return incomeWithApy;
    }

   
    @Override
    public List<IncomeWithApyAllDate> calculateIncomeWithApyAllDate(CalculateIncomeWithApyRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }
        if (request.getIndexs() == null || request.getType() == null || request.getQuantity() == null) {
            throw new IllegalArgumentException("Request parameters must not be null");
        }

        LocalDate fromDate = LocalDate.parse(request.getFrom());
        LocalDate toDate = LocalDate.parse(request.getTo());

        List<IncomeWithApyAllDate> result = new ArrayList<>();
        LocalDate currentDate = fromDate;

        while (!currentDate.isAfter(toDate)) {
            LocalDate nextDate = TimePeriods.getAnalyze(TimePeriods.valueOf(request.getType().toUpperCase()), currentDate, request.getQuantity());

            
            List<CsvData> dataList = csvDataRepository.findAllByUploadInfoIdDateBetween(currentDate, nextDate);

            if (!dataList.isEmpty()) {
                CsvData firstData = dataList.stream().min(Comparator.comparing(d -> d.getUploadInfoId().getDate())).orElse(null);
                CsvData lastData = dataList.stream().max(Comparator.comparing(d -> d.getUploadInfoId().getDate())).orElse(null);

                if (firstData != null && lastData != null) {
                    IncomeWithApyAllDate income = new IncomeWithApyAllDate();
                    income.setSource(firstData.getUploadInfoId().getSource());
                    income.setHistoryFrom(currentDate);
                    income.setHistoryTo(nextDate);
                    income.setFrom(firstData.getUploadInfoId().getDate().toString());
                    income.setTo(lastData.getUploadInfoId().getDate().toString());
                    income.setPurchaseAmount(firstData.getLow());
                    income.setSaleAmount(lastData.getHigh());
                    income.setIncome(income.getSaleAmount() - income.getPurchaseAmount());

                    double daysDifference = firstData.getUploadInfoId().getDate().until(lastData.getUploadInfoId().getDate(), java.time.temporal.ChronoUnit.DAYS);
                    double yearsDifference = daysDifference / 365.0;

                    double apy = Math.pow(income.getSaleAmount() / income.getPurchaseAmount(), 1.0 / yearsDifference) - 1;
                    income.setApy(apy);

                    result.add(income);
                    
                    
                    System.out.println("Current Date: " + currentDate);
                    System.out.println("Next Date: " + nextDate);
                   
                    System.out.println("Data List Size: " + dataList.size());
                }
            }

            currentDate = nextDate;  
        }

        return result;
    }

   

   
    @Override
    public List<IncomeWithIrr> calculateIncomeWithIrr(CalculateIncomeWithApyRequest request) {
        // TODO Auto-generated method stub
        return null;
    }
 

    @Override
    public String calculateCorrelation(CorrelationRequest correlationRequest) {
    	if (correlationRequest == null || correlationRequest.getIndexs() == null || correlationRequest.getIndexs().size() < 2) {
            throw new IllegalArgumentException("Two indices are required to calculate correlation.");
        }

        String index1 = correlationRequest.getIndexs().get(0);
        String index2 = correlationRequest.getIndexs().get(1);
        LocalDate from = LocalDate.parse(correlationRequest.getFrom());
        LocalDate to = LocalDate.parse(correlationRequest.getTo());
		return index2;

		/*
		 * 
		 * List<CsvData> data1 =
		 * csvDataRepository.findAllByUploadInfoSourceAndUploadInfoDateBetween(index1,
		 * from, to); List<CsvData> data2 =
		 * csvDataRepository.findAllByUploadInfoSourceAndUploadInfoDateBetween(index2,
		 * from, to);
		 */
		/*
		 * if (data1.isEmpty() || data2.isEmpty()) { return
		 * "No data available for one or both indices in the given period."; }
		 * 
		 * 
		 * double[] values1 = data1.stream().mapToDouble(CsvData::getClose).toArray();
		 * double[] values2 = data2.stream().mapToDouble(CsvData::getClose).toArray();
		 */

        
		/*
		 * PearsonsCorrelation correlation = new PearsonsCorrelation(); double
		 * correlationCoefficient = correlation.correlation(values1, values2);
		 * 
		 * 
		 * String correlationDescription; if (Math.abs(correlationCoefficient) > 0.8) {
		 * correlationDescription = (correlationCoefficient > 0) ?
		 * "Very strong positive correlation" : "Very strong negative correlation"; }
		 * else if (Math.abs(correlationCoefficient) > 0.6) { correlationDescription =
		 * (correlationCoefficient > 0) ? "Strong positive correlation" :
		 * "Strong negative correlation"; } else if (Math.abs(correlationCoefficient) >
		 * 0.4) { correlationDescription = (correlationCoefficient > 0) ?
		 * "Moderate positive correlation" : "Moderate negative correlation"; } else if
		 * (Math.abs(correlationCoefficient) > 0.2) { correlationDescription =
		 * (correlationCoefficient > 0) ? "Weak positive correlation" :
		 * "Weak negative correlation"; } else if (Math.abs(correlationCoefficient) >
		 * 0.1) { correlationDescription = (correlationCoefficient > 0) ?
		 * "Negligible positive correlation" : "Negligible negative correlation"; } else
		 * { correlationDescription = "No correlation"; }
		 * 
		 * return correlationDescription;
		 */
    
    }

    @Transactional
    public Boolean deleteAllHistoryForCompany(String indexName) {
        try {
            List<UploadInfo> uploadInfoList = uploadInfoRepository.findAllBySource(indexName);

            if (uploadInfoList.isEmpty()) {
                System.out.println("No data found for deletion for index: " + indexName);
                return false;
            }

            String sourceToDelete = uploadInfoList.get(0).getSource();

            if (sourceToDelete == null) {
                System.out.println("Source is null for index: " + indexName);
                return false;
            }

            csvDataRepository.deleteAllByUploadInfoId_Source(sourceToDelete);
            uploadInfoRepository.deleteAllBySource(sourceToDelete);

            System.out.println("Successfully deleted history for index: " + indexName);
            return true;
        } catch (Exception e) {
            System.out.println("Error deleting history for index: " + indexName);
            e.printStackTrace();
            return false;
        }
    }


}
