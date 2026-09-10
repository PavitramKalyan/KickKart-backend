package com.kickkart.repository;

import com.kickkart.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductId(Long productId);

    Optional<ProductImage> findFirstByProductId(Long productId);

    List<ProductImage> findByProductIdIn(List<Long> productIds);
}
