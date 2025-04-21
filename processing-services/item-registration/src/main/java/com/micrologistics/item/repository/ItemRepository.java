package com.micrologistics.item.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.micrologistics.item.entity.Item;

/**
 * Repository interface for managing Item entities.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, String> {
    
    /**
     * Find an item by its tracking ID.
     * 
     * @param trackingId The tracking ID
     * @return An Optional containing the item if found
     */
    Optional<Item> findByTrackingId(String trackingId);
    
    /**
     * Find items by their status.
     * 
     * @param status The status to filter by
     * @return A list of items with the specified status
     */
    List<Item> findByStatus(String status);
    
    /**
     * Find items by their status with pagination.
     * 
     * @param status The status to filter by
     * @param pageable Pagination information
     * @return A page of items with the specified status
     */
    Page<Item> findByStatus(String status, Pageable pageable);
    
    /**
     * Find items by their destination.
     * 
     * @param destination The destination to filter by
     * @return A list of items with the specified destination
     */
    List<Item> findByDestination(String destination);
    
    /**
     * Find items by their status and destination.
     * 
     * @param status The status to filter by
     * @param destination The destination to filter by
     * @return A list of items with the specified status and destination
     */
    List<Item> findByStatusAndDestination(String status, String destination);
    
    /**
     * Check if an item exists with the given tracking ID.
     * 
     * @param trackingId The tracking ID
     * @return True if an item exists with the tracking ID
     */
    boolean existsByTrackingId(String trackingId);
    
    /**
     * Find items with a weight greater than the specified value.
     * 
     * @param weight The minimum weight
     * @return A list of items with a weight greater than the specified value
     */
    List<Item> findByWeightGreaterThan(Double weight);
    
    /**
     * Count the number of items by status.
     * 
     * @param status The status to filter by
     * @return The number of items with the specified status
     */
    long countByStatus(String status);
    
    /**
     * Calculate average weight by destination.
     * 
     * @return List of average weights per destination
     */
    @Query("SELECT i.destination, AVG(i.weight) FROM Item i GROUP BY i.destination")
    List<Object[]> findAverageWeightByDestination();
}
