package com.micrologistics.container.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.micrologistics.container.entity.Container;

/**
 * Repository interface for managing Container entities.
 */
@Repository
public interface ContainerRepository extends JpaRepository<Container, String> {
    
    /**
     * Find a container by its container number.
     * 
     * @param containerNumber The container number
     * @return An Optional containing the container if found
     */
    Optional<Container> findByContainerNumber(String containerNumber);
    
    /**
     * Find containers by their status.
     * 
     * @param status The status to filter by
     * @return A list of containers with the specified status
     */
    List<Container> findByStatus(String status);
    
    /**
     * Find containers by their destination.
     * 
     * @param destination The destination to filter by
     * @return A list of containers with the specified destination
     */
    List<Container> findByDestination(String destination);
    
    /**
     * Find containers by their status and destination.
     * 
     * @param status The status to filter by
     * @param destination The destination to filter by
     * @return A list of containers with the specified status and destination
     */
    List<Container> findByStatusAndDestination(String status, String destination);
    
    /**
     * Find containers by their status with pagination.
     * 
     * @param status The status to filter by
     * @param pageable Pagination information
     * @return A page of containers with the specified status
     */
    Page<Container> findByStatus(String status, Pageable pageable);
    
    /**
     * Find containers by their destination with pagination.
     * 
     * @param destination The destination to filter by
     * @param pageable Pagination information
     * @return A page of containers with the specified destination
     */
    Page<Container> findByDestination(String destination, Pageable pageable);
    
    /**
     * Find containers by their status and destination with pagination.
     * 
     * @param status The status to filter by
     * @param destination The destination to filter by
     * @param pageable Pagination information
     * @return A page of containers with the specified status and destination
     */
    Page<Container> findByStatusAndDestination(String status, String destination, Pageable pageable);
    
    /**
     * Find containers that contain a specific item.
     * 
     * @param itemId The item ID to look for
     * @return A list of containers that contain the specified item
     */
    @Query("SELECT c FROM Container c JOIN c.items i WHERE i.itemId = :itemId")
    List<Container> findContainersContainingItem(String itemId);
    
    /**
     * Find available containers for loading items to a specific destination.
     * 
     * @param destination The destination
     * @return A list of containers that are available for loading
     */
    @Query("SELECT c FROM Container c WHERE c.destination = :destination AND c.status IN ('CREATED', 'LOADING')")
    List<Container> findAvailableContainersForDestination(String destination);
    
    /**
     * Find containers that were dispatched within a time range.
     * 
     * @param startTime The start time
     * @param endTime The end time
     * @return A list of containers dispatched within the specified time range
     */
    List<Container> findByDispatchedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find containers with high utilization (over 90% of either weight or volume capacity).
     * 
     * @return A list of highly utilized containers
     */
    @Query("SELECT c FROM Container c WHERE " +
           "(c.currentWeight / c.maxWeight > 0.9 OR c.currentVolume / c.maxVolume > 0.9) " +
           "AND c.status IN ('LOADING', 'CLOSED')")
    List<Container> findContainersWithHighUtilization();
    
    /**
     * Count containers by status.
     * 
     * @param status The status to count
     * @return The number of containers with the specified status
     */
    long countByStatus(String status);
    
    /**
     * Check if a container exists with the given container number.
     * 
     * @param containerNumber The container number
     * @return True if a container exists with the specified container number
     */
    boolean existsByContainerNumber(String containerNumber);
    
    /**
     * Get the average volume utilization by destination.
     * 
     * @return A list of destinations and their average volume utilizations
     */
    @Query("SELECT c.destination, AVG(c.currentVolume / c.maxVolume * 100) FROM Container c " +
           "WHERE c.status IN ('CLOSED', 'DISPATCHED', 'DELIVERED') " +
           "GROUP BY c.destination")
    List<Object[]> getAverageVolumeUtilizationByDestination();
    
    /**
     * Get the average weight utilization by destination.
     * 
     * @return A list of destinations and their average weight utilizations
     */
    @Query("SELECT c.destination, AVG(c.currentWeight / c.maxWeight * 100) FROM Container c " +
           "WHERE c.status IN ('CLOSED', 'DISPATCHED', 'DELIVERED') " +
           "GROUP BY c.destination")
    List<Object[]> getAverageWeightUtilizationByDestination();
}
