package com.greglmx.wishly.repository;

import com.greglmx.wishly.model.Gift;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface GiftRepository extends CrudRepository<Gift, Long> {
	List<Gift> findByWishlistId(Long wishlistId);
	boolean existsByWishlistIdAndNameIgnoreCase(Long wishlistId, String name);
}
