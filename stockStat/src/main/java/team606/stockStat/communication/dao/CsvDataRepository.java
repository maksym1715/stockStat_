package team606.stockStat.communication.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import team606.stockStat.communication.parser.CsvData;
import team606.stockStat.communication.parser.UploadInfo;

import java.util.Date;
import java.util.List;

public interface CsvDataRepository extends JpaRepository<CsvData, Long> {
    List<CsvData> findAllByUploadInfoIdIn(List<UploadInfo>ids);

    void deleteAllByUploadInfoId_Source(String source);

	
	
}
