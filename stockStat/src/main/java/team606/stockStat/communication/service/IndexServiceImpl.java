package team606.stockStat.communication.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import team606.stockStat.communication.dao.CsvDataRepository;
import team606.stockStat.communication.dao.UploadInfoRepository;
import team606.stockStat.communication.parser.CsvData;
import team606.stockStat.communication.parser.UploadInfo;

@Service
public class IndexServiceImpl implements IndexService {

    private final CsvDataRepository csvDataRepository;
    private final UploadInfoRepository uploadInfoRepository;

    public IndexServiceImpl(CsvDataRepository csvDataRepository, UploadInfoRepository uploadInfoRepository) {
        this.csvDataRepository = csvDataRepository;
        this.uploadInfoRepository = uploadInfoRepository;
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


    }

	

	

}
