package com.micrologistics.routing.algorithm;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**

Tests for the OptimalPathFinder algorithm.
*/
class OptimalPathFinderTest {

private OptimalPathFinder pathFinder;

@BeforeEach
void setUp() {
pathFinder = new OptimalPathFinder();
}

@Test
void findOptimalPath_ReturnsValidPath() {
// Arrange
String itemId = "item1";
String destination = "New York";
double weight = 15.0;
int priority = 1;

// Act
List<String> path = pathFinder.findOptimalPath(itemId, destination, weight, priority);

// Assert
assertTrue(path.contains("OUTBOUND_DOCK_WEST"), 
        "West coast destinations should use the west outbound dock");

@Test
void findOptimalPath_WithNonOperationalEquipment_AvoidsThatEquipment() {
// Arrange
String itemId = "item1";
String destination = "New York";
double weight = 15.0;
int priority = 1;

// Make SORTING_AREA_A non-operational
pathFinder.updateEquipmentStatus("SORTING_AREA_A", false);

// Act
List<String> path = pathFinder.findOptimalPath(itemId, destination, weight, priority);

// Assert
assertFalse(path.contains("SORTING_AREA_A"), 
        "Path should not include non-operational equipment");
assertTrue(path.contains("SORTING_AREA_B"), 
        "Path should use alternative equipment when primary is non-operational");


@Test
void calculateEstimatedTime_ReturnsPositiveValue() {
// Arrange
List<String> path = List.of("INBOUND_DOCK", "SCANNER_STATION", "SORTING_AREA_A", "PACKAGING_AREA");



// Act
double estimatedTime = pathFinder.calculateEstimatedTime(path);

// Assert
assertTrue(estimatedTime > 0, "Estimated time should be positive");

// Act
double estimatedTime = pathFinder.calculateEstimatedTime(path);

// Assert
assertTrue(estimatedTime > 0, "Estimated time should be positive");


@Test
void calculateEstimatedTime_LongerPath_TakesMoreTime() {
// Arrange
List<String> shortPath = List.of("INBOUND_DOCK", "SCANNER_STATION", "OUTBOUND_DOCK_NORTH");
List<String> longPath = List.of("INBOUND_DOCK", "SCANNER_STATION", "SORTING_AREA_A",
"PACKAGING_AREA", "CONTAINER_LOADING", "OUTBOUND_DOCK_NORTH");

// Act
double shortPathTime = pathFinder.calculateEstimatedTime(shortPath);
double longPathTime = pathFinder.calculateEstimatedTime(longPath);

// Assert
assertTrue(longPathTime > shortPathTime, 
        "Longer paths should have longer estimated times");
@Test
void getEquipmentLoadStatus_ReturnsNonEmptyMap() {
// Act
Map<String, Integer> loadStatus = pathFinder.getEquipmentLoadStatus();

// Assert
assertNotNull(loadStatus);
assertFalse(loadStatus.isEmpty());

// All load values should be between 0 and 100
for (Integer load : loadStatus.values()) {
    assertTrue(load >= 0 && load <= 100, 
            "Load values should be between 0 and 100");
}


@Test
void updateEquipmentStatus_NonOperational_SetsLoadToMax() {
// Arrange
String equipment = "SCANNER_STATION";
pathFinder.updateEquipmentStatus(equipment, true); // Ensure it's operational first

// Act
pathFinder.updateEquipmentStatus(equipment, false); // Make non-operational
Map<String, Integer> loadStatus = pathFinder.getEquipmentLoadStatus();
Map<String, Boolean> operationalStatus = pathFinder.getEquipmentOperationalStatus();

// Assert
assertEquals(100, loadStatus.get(equipment), 
        "Non-operational equipment should have maximum load");
assertFalse(operationalStatus.get(equipment), 
        "Equipment should be marked as non-operational");


@Test
void getAllFacilities_ReturnsExpectedFacilities() {
// Act
List<String> facilities = pathFinder.getAllFacilities();

// Assert
assertNotNull(facilities);
assertFalse(facilities.isEmpty());
assertTrue(facilities.contains("INBOUND_DOCK"));
assertTrue(facilities.contains("SCANNER_STATION"));
assertTrue(facilities.contains("SORTING_AREA_A"));
assertTrue(facilities.contains("SORTING_AREA_B"));
assertTrue(facilities.contains("OUTBOUND_DOCK_NORTH"));
assertTrue(facilities.contains("OUTBOUND_DOCK_SOUTH"));


// Assert
assertNotNull(facilities);
assertFalse(facilities.isEmpty());
assertTrue(facilities.contains("INBOUND_DOCK"));
assertTrue(facilities.contains("SCANNER_STATION"));
assertTrue(facilities.contains("SORTING_AREA_A"));
assertTrue(facilities.contains("SORTING_AREA_B"));
assertTrue(facilities.contains("OUTBOUND_DOCK_NORTH"));
assertTrue(facilities.contains("OUTBOUND_DOCK_SOUTH"));


@Test
void extractRegion_CorrectlyIdentifiesRegions() {
// Using reflection to access private method for testing
// This is a special case where testing a private method directly makes sense
// because it's complex logic that's core to the algorithm
	
	 // We test this indirectly through the public methods
	 
	 // Act & Assert
	 // Test through findOptimalPath which uses extractRegion internally
	 List<String> northPath = pathFinder.findOptimalPath("item1", "North Dakota", 10.0, 1);
	 assertTrue(northPath.contains("OUTBOUND_DOCK_NORTH"), 
	         "North destinations should route to north dock");
	 
	 List<String> southPath = pathFinder.findOptimalPath("item2", "South Carolina", 10.0, 1);
	 assertTrue(southPath.contains("OUTBOUND_DOCK_SOUTH"), 
	         "South destinations should route to south dock");
	 
	 List<String> eastPath = pathFinder.findOptimalPath("item3", "East Hampton", 10.0, 1);
	 assertTrue(eastPath.contains("OUTBOUND_DOCK_EAST"), 
	         "East destinations should route to east dock");
	 
	 List<String> westPath = pathFinder.findOptimalPath("item4", "West Hollywood", 10.0, 1);
	 assertTrue(westPath.contains("OUTBOUND_DOCK_WEST"), 
	         "West destinations should route to west dock");
}
};

