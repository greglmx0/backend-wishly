package com.greglmx.wishly.repository;

import com.greglmx.wishly.model.Gift;
import org.springframework.data.repository.CrudRepository;

public interface GiftRepository extends CrudRepository<Gift, Long> {
}
