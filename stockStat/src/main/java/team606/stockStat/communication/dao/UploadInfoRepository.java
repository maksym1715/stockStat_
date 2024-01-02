package team606.stockStat.communication.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import team606.stockStat.communication.parser.UploadInfo;

import java.time.LocalDate;
import java.util.List;

public interface UploadInfoRepository extends JpaRepository<UploadInfo, Long> {


    List<UploadInfo> findAllBySourceInAndDateIsAfterAndDateIsBefore(List<String> source, LocalDate one,LocalDate second);
    
    List<UploadInfo>findAllBySource(String source);
    @Query("SELECT DISTINCT source FROM UploadInfo")
    List<String> findAllDistinctIndexes();

}
