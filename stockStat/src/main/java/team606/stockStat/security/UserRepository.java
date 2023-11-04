package team606.stockStat.security;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String>{
    User findUserByUsername(String username);

	
}
