package com.clearance_hooter.clearance_hooter_services.features.users;

import com.clearance_hooter.clearance_hooter_services.dto.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
}
