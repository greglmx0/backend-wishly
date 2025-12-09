package com.greglmx.wishly.repository;
import org.springframework.data.repository.CrudRepository;

import com.greglmx.wishly.model.User;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface UserRepository extends CrudRepository<User, Integer> {
    User findById(long id);
    User findByUsername(String username);
    User findByEmail(String email);
    User findByEmailIgnoreCase(String email);
}