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

import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import team606.stockStat.communication.dao.CsvDataRepository;
import team606.stockStat.communication.dao.TimePeriods;
import team606.stockStat.communication.dao.UploadInfoRepository;
import team606.stockStat.communication.dto.CalculateIncomeWithApyRequest;
import team606.stockStat.communication.dto.CalculateSumPackageRequest;
import team606.stockStat.communication.dto.CorrelationRequest;
import team606.stockStat.communication.dto.IncomeWithApy;
import team606.stockStat.communication.dto.IncomeWithIrr;
import team606.stockStat.communication.dto.PeriodData;
import team606.stockStat.communication.dto.PeriodDataCloseBetween;
import team606.stockStat.communication.dto.PeriodRequest;
import team606.stockStat.communication.dto.ResponseDto;
import team606.stockStat.communication.dto.TimeHistoryData;
import team606.stockStat.communication.parser.CsvData;
import team606.stockStat.communication.parser.SourceData;
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

	    List<IncomeWithApy> result = new ArrayList<>();

	    // Extract parameters from the request
	    List<String> indexs = request.getIndexs();
	    TimePeriods type = TimePeriods.valueOf(request.getType().toUpperCase());
	    LocalDate from = LocalDate.parse(request.getFrom());
	    LocalDate to = LocalDate.parse(request.getTo());
	    Long quantity = request.getQuantity();
	    
	    to = to.plusDays(1);

	    // Get data for indexes and period from the repository
	    List<ResponseDto> data = getAllDataBySources(type, indexs, from, to, quantity);

	    LocalDate toDate = to; // Create a copy of the 'to' variable
	    ResponseDto minDto = Collections.min(data, Comparator.comparingDouble(dto -> calculateApy(dto.getMax() - dto.getMin(), dto.getMean(), from, toDate)));
	    ResponseDto maxDto = Collections.max(data, Comparator.comparingDouble(dto -> calculateApy(dto.getMax() - dto.getMin(), dto.getMean(), from, toDate)));

	    // Create IncomeWithApy objects for minIncome and maxIncome
	    IncomeWithApy minIncome = createIncomeWithApy(minDto, from, to);
	    IncomeWithApy maxIncome = createIncomeWithApy(maxDto, from, to);

	    // Add minIncome and maxIncome to the result
	    result.add(minIncome);
	    result.add(maxIncome);

	    // Return the result
	    return result;
	}

	//  IncomeWithApy
	private IncomeWithApy createIncomeWithApy(ResponseDto dto, LocalDate from, LocalDate to) {
	    IncomeWithApy incomeWithApy = new IncomeWithApy();
	    incomeWithApy.setSource(dto.getSource());
	    incomeWithApy.setHistoryFrom(dto.getFrom());
	    incomeWithApy.setHistoryTo(dto.getTo());
	    incomeWithApy.setType(dto.getType());
	    incomeWithApy.setFrom(from.toString());
	    incomeWithApy.setTo(to.toString());
	    double income = dto.getMax() - dto.getMin();
	    incomeWithApy.setIncome(income);
	    double apy = calculateApy(income, dto.getMean(), from, to);
	    incomeWithApy.setApy(apy);
	    return incomeWithApy;
	}
	
	

	// Method to calculate APY (Annual Percentage Yield)
	private double calculateApy(double income, double mean, LocalDate from, LocalDate to) {
		// Calculate the number of days between from and to
	    long days = ChronoUnit.DAYS.between(from, to);
	    
	 // Check if days is greater than 0 to avoid division by zero
	    if (days <= 0) {
	        throw new IllegalArgumentException("Invalid date range: 'from' date must be before 'to' date");
	    }

	 // Calculate APY
	    double apy = (income / mean) / (days / 365.0); // Divide by 365 to get APY

	    return apy;
	}

	
	
	
	
	@Override
	public List<IncomeWithApy> calculateIncomeWithApyAllDate(CalculateIncomeWithApyRequest request) {
	    if (request == null) {
	        throw new IllegalArgumentException("Request must not be null");
	    }
	    if (request.getIndexs() == null || request.getType() == null || request.getQuantity() == null) {
	        throw new IllegalArgumentException("Request parameters must not be null");
	    }

	    List<IncomeWithApy> result = new ArrayList<>();

	    // Extract parameters from the request
	    List<String> indexs = request.getIndexs();
	    TimePeriods type = TimePeriods.valueOf(request.getType().toUpperCase());
	    Long quantity = request.getQuantity();

	    // Get the current date
	    LocalDate currentDate = LocalDate.now();

	    // Retrieve data for indexes and period from the repository
	    List<ResponseDto> data = getAllDataBySources(type, indexs, null, currentDate, quantity);

	    // Iterate over each item and calculate income with APY
	    for (ResponseDto dto : data) {
	        // Calculate income and APY
	        double income = dto.getMax() - dto.getMin();
	        double apy = calculateApy(income, dto.getMean(), null, currentDate);

	        // Create an IncomeWithApy object and add it to the result
	        IncomeWithApy incomeWithApy = new IncomeWithApy();
	        incomeWithApy.setSource(dto.getSource());
	        incomeWithApy.setHistoryFrom(dto.getFrom());
	        incomeWithApy.setHistoryTo(dto.getTo());
	        incomeWithApy.setType(dto.getType());
	        incomeWithApy.setFrom(null); // No need to set 'from' for all dates
	        incomeWithApy.setTo(currentDate.toString());
	        incomeWithApy.setIncome(income);
	        incomeWithApy.setApy(apy);
	        incomeWithApy.setPurchaseAmount(dto.getMin()); // Set minimum purchase amount
	        incomeWithApy.setSaleAmount(dto.getMax()); // Set maximum sale amount

	        result.add(incomeWithApy);
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
		

		return "very strong positive correlation";

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
