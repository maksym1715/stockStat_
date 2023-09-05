package team606.stockStat.communication.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import team606.stockStat.communication.parser.CsvData;

public interface CsvDataRepository extends JpaRepository<CsvData, Long> {
    
}
