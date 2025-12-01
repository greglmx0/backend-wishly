package com.greglmx.wishly.repository;

import com.greglmx.wishly.model.Wishlist;
import org.springframework.data.repository.CrudRepository;
import java.util.ArrayList;

public interface WishlistRepository extends CrudRepository<Wishlist, Long> {
    public boolean existsByOwnerAndName(com.greglmx.wishly.model.User owner, String name);
    public ArrayList<Wishlist> findByOwnerId(Long ownerId);
}
