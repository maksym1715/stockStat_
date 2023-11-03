package team606.stockStat.accounting.dao;

import org.springframework.data.mongodb.repository.MongoRepository;


import team606.stockStat.accounting.model.UserAccount;

public interface UserAccountRepository extends MongoRepository<UserAccount, String>{
	 
	
}
