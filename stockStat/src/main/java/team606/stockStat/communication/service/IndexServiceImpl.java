package team606.stockStat.communication.service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import team606.stockStat.communication.dao.CsvDataRepository;
import team606.stockStat.communication.dao.TimePeriods;
import team606.stockStat.communication.dao.UploadInfoRepository;
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

//        return books.stream().collect(Collectors.toMap(Book::getIsbn, Book::getName));

    @Override
    public List<CsvData> getAllDataBySources(TimePeriods timePeriods, List<String> source, LocalDate from, LocalDate to, Long quantity) {
        Map<String, List<UploadInfo>> allBySource =
                uploadInfoRepository.findAllBySourceInAndDateIsAfterAndDateIsBefore(source, from, to)
                        .stream()
                        .collect(Collectors.groupingBy(UploadInfo::getSource));

        Map<String,ResponseDto>result = new HashMap<>();
        for (Map.Entry<String, List<UploadInfo>> uploadInfo : allBySource.entrySet()) {
            Map<LocalDate, CsvData> allByUploadInfoIdIn = csvDataRepository.findAllByUploadInfoIdIn(uploadInfo.getValue())
                    .stream()
                    .collect(Collectors.toMap(a -> a.getUploadInfoId().getDate(), b -> b));
           // check that all maps is sorted here according to date.
            for (Map.Entry<LocalDate, CsvData> objectObjectEntry : allByUploadInfoIdIn.entrySet()) {
                LocalDate firstDate = objectObjectEntry.getValue().getUploadInfoId().getDate();
                LocalDate secondDate = TimePeriods.getAnalyze(timePeriods, firstDate, quantity);
                CsvData firstData = objectObjectEntry.getValue();
                CsvData csvDataLastPeriod = allByUploadInfoIdIn.get(secondDate);
                //make calculation and save it in data structire and then make comparing. In firstDate we take min price and in second the max value(for max profit case)
            }
        }
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

        //1. You should add query where you find all unique values in uploadInfo table.
        //2. Pay attention that you should get all unique values, for example you have: BSS,BSS,AAA,QQQ -
        //from this values you need only unique values.
        List<String> uniqueIndexes = uploadInfoRepository.findAllDistinctIndexes();
        return uniqueIndexes;

    }


}
