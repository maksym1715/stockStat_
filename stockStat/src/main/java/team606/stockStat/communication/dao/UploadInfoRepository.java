package team606.stockStat.communication.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import team606.stockStat.communication.parser.UploadInfo;

public interface UploadInfoRepository extends JpaRepository<UploadInfo, Long> {
    
}
