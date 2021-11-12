package com.isaccanedo.springboot.azure;

import org.springframework.data.repository.CrudRepository;

/**
 * @author aiet
 */
public interface UserRepository extends CrudRepository<User, Long> {
}
