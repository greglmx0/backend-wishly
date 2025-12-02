package com.greglmx.wishly.repository;

import com.greglmx.wishly.model.Wishlist;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface WishlistRepository extends CrudRepository<Wishlist, Long> {
    public boolean existsByOwnerIdAndName(Long ownerId, String name);
    public List<Wishlist> findByOwnerId(Long ownerId);
}
