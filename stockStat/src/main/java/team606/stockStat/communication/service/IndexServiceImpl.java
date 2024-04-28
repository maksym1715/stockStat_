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

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;
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
                responseDto.setApy(calculateApy(income.getLow(), income.getHigh(), quantity, timePeriods));
                result.add(responseDto);
            }
        }
        return result;
    }

    public static double calculateIRR(double[] cashFlows) {
        UnivariateDifferentiableFunction function = new UnivariateDifferentiableFunction() {
            @Override
            public double value(double r) {
                return npv(cashFlows, r);
            }

            @Override
            public DerivativeStructure value(DerivativeStructure t) {
                double npv = 0;
                double dnpv = 0; // First derivative of NPV
                for (int i = 0; i < cashFlows.length; i++) {
                    npv += cashFlows[i] / Math.pow(1 + t.getValue(), i);
                    dnpv -= i * cashFlows[i] / Math.pow(1 + t.getValue(), i + 1);
                }
                return t.compose(npv, dnpv);
            }
        };

        NewtonRaphsonSolver solver = new NewtonRaphsonSolver(1e-6);
        double initialGuess = 0.10;  // Start with an initial guess of 10%
        double lowerBound = -0.99;   // Set a reasonable lower bound for IRR
        double upperBound = 1.0;     // Upper bound
        return solver.solve(100, function, initialGuess, lowerBound, upperBound);
    }

    private static double npv(double[] cashFlows, double r) {
        double npv = 0;
        for (int i = 0; i < cashFlows.length; i++) {
            npv += cashFlows[i] / Math.pow(1 + r, i);
        }
        return npv;
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
                        if (currentDifference > 0 && currentDifference > maxDifference) {
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

    //TODO to add function for calculation min.
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
            Map<LocalDate, CsvData> localDateCsvDataMap = dataList.stream().collect(Collectors.toMap(a -> a.getUploadInfoId().getDate(), a -> a, (c, v) -> v));
            double maxDifference = 0;
            LocalDate dateOfPurchaseFormaxValue = null;
            LocalDate dateofSaleForMaxValue = null;
            double purchaseAmmountForMaxValue = 0;
            double saleAmmountForMaxValue = 0;
            double incomeForMaxValue = 0;

            double minDifference = Long.MAX_VALUE;
            LocalDate dateOfPurchaseForMinValue = null;
            LocalDate dateofSaleForMinValue = null;
            double purchaseAmmountForMinValue = 0;
            double saleAmmountForMinValue = 0;
            double incomeForMinValue = 0;
            double apy = 0;

            for (int i = 0; i < dataList.size(); i++) {
                CsvData minData = dataList.get(i);
                LocalDate minDate = minData.getUploadInfoId().getDate();
                LocalDate nextDate = TimePeriods.getAnalyze(timePeriodType, minDate, quantity);
                CsvData nextDateCsv = localDateCsvDataMap.get(nextDate);

                double currentDifferenceForMax = nextDateCsv.getHigh() - minData.getLow();
                if (currentDifferenceForMax > 0 && currentDifferenceForMax > maxDifference) {
                    maxDifference = currentDifferenceForMax;
                    dateOfPurchaseFormaxValue = minDate;
                    dateofSaleForMaxValue = nextDate;
                    purchaseAmmountForMaxValue = minData.getLow();
                    saleAmmountForMaxValue = nextDateCsv.getHigh();
                    incomeForMaxValue = currentDifferenceForMax;
                }
                double currentDifferenceForMin = nextDateCsv.getHigh() - minData.getLow();
                if (currentDifferenceForMin > 0 && currentDifferenceForMin < minDifference) {
                    minDifference = currentDifferenceForMin;
                    dateOfPurchaseForMinValue = minDate;
                    dateofSaleForMinValue = nextDate;
                    purchaseAmmountForMinValue = minData.getLow();
                    saleAmmountForMinValue = nextDateCsv.getHigh();
                    incomeForMinValue = currentDifferenceForMax;
                }
            }
            result.add(new IncomeWithApy(dateOfPurchaseForMinValue, purchaseAmmountForMinValue, dateofSaleForMinValue, saleAmmountForMinValue, incomeForMinValue, calculateApy(purchaseAmmountForMinValue, saleAmmountForMinValue, quantity, timePeriodType)));
            result.add(new IncomeWithApy(dateOfPurchaseFormaxValue, purchaseAmmountForMaxValue, dateofSaleForMaxValue, saleAmmountForMaxValue, incomeForMaxValue, calculateApy(purchaseAmmountForMaxValue, saleAmmountForMaxValue, quantity, timePeriodType)));
        }


//        if (bestMinIndex >= 0 && bestMaxIndex >= 0) {
//            CsvData minCsvData = dataList.get(bestMinIndex);
//            CsvData maxCsvData = dataList.get(bestMaxIndex);
//
//            LocalDate analyzeDate = TimePeriods.getAnalyze(timePeriodType, minCsvData.getUploadInfoId().getDate(), request.getQuantity());
//
//            IncomeWithApy minIncome = createIncomeWithApyFromCsvData(minCsvData, analyzeDate);
//            IncomeWithApy maxIncome = createIncomeWithApyFromCsvData(maxCsvData, analyzeDate);
//
//            result.add(minIncome);
//            result.add(maxIncome);
//        }

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

        List<IncomeWithApyAllDate> result = new ArrayList<>();
        for (Map.Entry<String, List<UploadInfo>> entry : dataBySource.entrySet()) {
            List<CsvData> dataList = csvDataRepository.findAllByUploadInfoIdIn(entry.getValue());
            Map<LocalDate, CsvData> localDateCsvDataMap = dataList.stream().collect(Collectors.toMap(a -> a.getUploadInfoId().getDate(), a -> a, (c, v) -> v));
            for (int i = 0; i < dataList.size(); i++) {
                CsvData minData = dataList.get(i);
                LocalDate minDate = minData.getUploadInfoId().getDate();
                LocalDate nextDate = TimePeriods.getAnalyze(timePeriodType, minDate, quantity);
                CsvData nextDateCsv = localDateCsvDataMap.get(nextDate);
                result.add(new IncomeWithApyAllDate(entry.getKey(), LocalDate.parse(request.getFrom()), LocalDate.parse(request.getTo()),
                        timePeriodType.toString(), minDate.toString(), nextDate.toString(),
                        minData.getLow(), nextDateCsv.getHigh(),
                        nextDateCsv.getHigh() - minData.getLow(), calculateApy(minData.getLow(), nextDateCsv.getHigh(), quantity, timePeriodType)));
            }
        }
        return result;
    }


//    if (request == null) {
//        throw new IllegalArgumentException("Request must not be null");
//    }
//    if (request.getIndexs() == null || request.getType() == null || request.getQuantity() == null) {
//        throw new IllegalArgumentException("Request parameters must not be null");
//    }
//
//    LocalDate fromDate = LocalDate.parse(request.getFrom());
//    LocalDate toDate = LocalDate.parse(request.getTo());
//
//    List<IncomeWithApyAllDate> result = new ArrayList<>();
//    LocalDate currentDate = fromDate;
//
//    while (!currentDate.isAfter(toDate)) {
//        LocalDate nextDate = TimePeriods.getAnalyze(TimePeriods.valueOf(request.getType().toUpperCase()), currentDate, request.getQuantity());
//
//
//        List<CsvData> dataList = csvDataRepository.findAllByUploadInfoIdDateBetween(currentDate, nextDate);
//
//        if (!dataList.isEmpty()) {
//            CsvData firstData = dataList.stream().min(Comparator.comparing(d -> d.getUploadInfoId().getDate())).orElse(null);
//            CsvData lastData = dataList.stream().max(Comparator.comparing(d -> d.getUploadInfoId().getDate())).orElse(null);
//
//            if (firstData != null && lastData != null) {
//                IncomeWithApyAllDate income = new IncomeWithApyAllDate();
//                income.setSource(firstData.getUploadInfoId().getSource());
//                income.setHistoryFrom(currentDate);
//                income.setHistoryTo(nextDate);
//                income.setFrom(firstData.getUploadInfoId().getDate().toString());
//                income.setTo(lastData.getUploadInfoId().getDate().toString());
//                income.setPurchaseAmount(firstData.getLow());
//                income.setSaleAmount(lastData.getHigh());
//                income.setIncome(income.getSaleAmount() - income.getPurchaseAmount());
//
//                double daysDifference = firstData.getUploadInfoId().getDate().until(lastData.getUploadInfoId().getDate(), java.time.temporal.ChronoUnit.DAYS);
//                double yearsDifference = daysDifference / 365.0;
//
//                double apy = Math.pow(income.getSaleAmount() / income.getPurchaseAmount(), 1.0 / yearsDifference) - 1;
//                income.setApy(apy);
//
//                result.add(income);
//
//
//                System.out.println("Current Date: " + currentDate);
//                System.out.println("Next Date: " + nextDate);
//
//                System.out.println("Data List Size: " + dataList.size());
//            }
//        }
//
//        currentDate = nextDate;
//    }
//
//    return result;


    @Override
    public List<IncomeWithIrr> calculateIncomeWithIrr(CalculateIncomeWithApyRequest request) {
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

        List<IncomeWithIrr> result = new ArrayList<>();

        for (Map.Entry<String, List<UploadInfo>> entry : dataBySource.entrySet()) {
            List<CsvData> dataList = csvDataRepository.findAllByUploadInfoIdIn(entry.getValue());
            Map<LocalDate, CsvData> localDateCsvDataMap = dataList.stream().collect(Collectors.toMap(a -> a.getUploadInfoId().getDate(), a -> a, (c, v) -> v));
            double maxDifference = 0;
            LocalDate dateOfPurchaseFormaxValue = null;
            LocalDate dateofSaleForMaxValue = null;
            double purchaseAmmountForMaxValue = 0;
            double saleAmmountForMaxValue = 0;
            double incomeForMaxValue = 0;

            double minDifference = Long.MAX_VALUE;
            LocalDate dateOfPurchaseForMinValue = null;
            LocalDate dateofSaleForMinValue = null;
            double purchaseAmmountForMinValue = 0;
            double saleAmmountForMinValue = 0;
            double incomeForMinValue = 0;
            double apy = 0;

            for (int i = 0; i < dataList.size(); i++) {
                CsvData minData = dataList.get(i);
                LocalDate minDate = minData.getUploadInfoId().getDate();
                LocalDate nextDate = TimePeriods.getAnalyze(timePeriodType, minDate, quantity);
                CsvData nextDateCsv = localDateCsvDataMap.get(nextDate);

                double currentDifferenceForMax = nextDateCsv.getHigh() - minData.getLow();
                if (currentDifferenceForMax > 0 && currentDifferenceForMax > maxDifference) {
                    maxDifference = currentDifferenceForMax;
                    dateOfPurchaseFormaxValue = minDate;
                    dateofSaleForMaxValue = nextDate;
                    purchaseAmmountForMaxValue = minData.getLow();
                    saleAmmountForMaxValue = nextDateCsv.getHigh();
                    incomeForMaxValue = currentDifferenceForMax;
                }
                double currentDifferenceForMin = nextDateCsv.getHigh() - minData.getLow();
                if (currentDifferenceForMin > 0 && currentDifferenceForMin < minDifference) {
                    minDifference = currentDifferenceForMin;
                    dateOfPurchaseForMinValue = minDate;
                    dateofSaleForMinValue = nextDate;
                    purchaseAmmountForMinValue = minData.getLow();
                    saleAmmountForMinValue = nextDateCsv.getHigh();
                    incomeForMinValue = currentDifferenceForMax;
                }
            }

            SubPeriodWithIrr subPeriodWithIrrForMin = new SubPeriodWithIrr(entry.getKey(), dateOfPurchaseForMinValue.toString(), purchaseAmmountForMinValue, dateofSaleForMinValue.toString(), saleAmmountForMinValue, incomeForMinValue, calculateIRR(new double[]{-purchaseAmmountForMinValue, 0, saleAmmountForMinValue}));
            SubPeriodWithIrr subPeriodWithIrrForMax = new SubPeriodWithIrr(entry.getKey(), dateOfPurchaseFormaxValue.toString(), purchaseAmmountForMaxValue, dateofSaleForMaxValue.toString(), saleAmmountForMaxValue, incomeForMaxValue, calculateIRR(new double[]{-purchaseAmmountForMaxValue, 0, saleAmmountForMaxValue}));
            result.add(new IncomeWithIrr(request.getFrom(), request.getTo(), request.getType(), request.getIndexs(), subPeriodWithIrrForMax, subPeriodWithIrrForMax));
        }
        return result;
    }


    @Override
    public String calculateCorrelation(CorrelationRequest request) {
        if (request == null || request.getIndexs() == null || request.getIndexs().size() < 2) {
            throw new IllegalArgumentException("Two indices are required to calculate correlation.");
        }
        // Check if the request and its parameters are present



        LocalDate fromDate = LocalDate.parse(request.getFrom());
        LocalDate toDate = LocalDate.parse(request.getTo()).plusDays(1);
        List<UploadInfo> uploadInfos = uploadInfoRepository.findAllBySourceInAndDateIsAfterAndDateIsBefore(
                request.getIndexs(), fromDate, toDate
        );
        Map<String, List<UploadInfo>> dataBySource = uploadInfos.stream()
                .sorted(Comparator.comparing(UploadInfo::getDate))
                .collect(Collectors.groupingBy(UploadInfo::getSource));
        List<UploadInfo> uploadInfos1 = dataBySource.get(request.getIndexs().get(0));
        List<UploadInfo> uploadInfos2 = dataBySource.get(request.getIndexs().get(1));
        double[] allByUploadInfoIdIn = csvDataRepository.findAllByUploadInfoIdIn(uploadInfos1).stream().map(a->a.getClose()).mapToDouble(Double::doubleValue).toArray();
        double[] allByUploadInfoIdIn1 = csvDataRepository.findAllByUploadInfoIdIn(uploadInfos2).stream().map(a->a.getClose()).mapToDouble(Double::doubleValue).toArray();
        double correlation = new PearsonsCorrelation().correlation(allByUploadInfoIdIn,allByUploadInfoIdIn1);
//        Strong Correlation: Generally, a correlation of -0.7 to -1.0 or 0.7 to 1.0 is considered strong. These values suggest a significant linear relationship between the variables.
//                Moderate Correlation: A correlation of -0.7 to -0.3 or 0.3 to 0.7 indicates a moderate linear relationship. This suggests some relationship, but it's not exceptionally tight.
//        Weak Correlation: A correlation of -0.3 to -0.1 or 0.1 to 0.3 suggests a weak linear relationship, which might be harder to use predictively as the scatter in the data points away from a line is considerable.
//                No Correlation: Very close to 0, from -0.1 to 0.1, indicates no noticeable linear relationship.
//

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
