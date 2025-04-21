package com.micrologistics.metrics.repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.micrologistics.metrics.entity.TimeSeriesData;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.timestreamwrite.TimestreamWriteClient;
import software.amazon.awssdk.services.timestreamwrite.model.Dimension;
import software.amazon.awssdk.services.timestreamwrite.model.MeasureValue;
import software.amazon.awssdk.services.timestreamwrite.model.MeasureValueType;
import software.amazon.awssdk.services.timestreamwrite.model.Record;
import software.amazon.awssdk.services.timestreamwrite.model.RejectedRecord;
import software.amazon.awssdk.services.timestreamwrite.model.RejectedRecordsException;
import software.amazon.awssdk.services.timestreamwrite.model.WriteRecordsRequest;
import software.amazon.awssdk.services.timestreamwrite.model.WriteRecordsResponse;

/**
 * Repository for storing time series data in Amazon Timestream.
 * This is a custom repository (not extending Spring Data JPA) for time series data.
 */
@Repository
@Slf4j
public class TimeSeriesRepository {
    
    @Value("${aws.timestream.database:micrologistics}")
    private String database;
    
    @Value("${aws.timestream.table:metrics}")
    private String table;
    
    @Value("${aws.timestream.enabled:false}")
    private boolean enabled;
    
    @Value("${aws.region:us-east-1}")
    private String awsRegion;
    
    private TimestreamWriteClient timestreamClient;
    
    /**
     * Constructor that initializes the Timestream client if enabled.
     */
    public TimeSeriesRepository() {
        if (enabled) {
            try {
                this.timestreamClient = TimestreamWriteClient.builder()
                        .region(Region.of(awsRegion))
                        .credentialsProvider(DefaultCredentialsProvider.create())
                        .build();
                log.info("Timestream client initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Timestream client", e);
                this.timestreamClient = null;
            }
        } else {
            log.info("Timestream integration is disabled");
        }
    }
    
    /**
     * Save a time series data point.
     * 
     * @param timeSeriesData The time series data to save
     * @return True if the operation was successful
     */
    public boolean save(TimeSeriesData timeSeriesData) {
        if (!enabled || timestreamClient == null) {
            log.debug("Timestream is disabled or client not initialized. Skipping save operation.");
            return false;
        }
        
        try {
            List<Record> records = new ArrayList<>();
            records.add(createRecord(timeSeriesData));
            
            WriteRecordsRequest writeRecordsRequest = WriteRecordsRequest.builder()
                    .databaseName(database)
                    .tableName(table)
                    .records(records)
                    .build();
            
            WriteRecordsResponse response = timestreamClient.writeRecords(writeRecordsRequest);
            log.debug("Successfully wrote time series data: {}", timeSeriesData);
            return true;
        } catch (RejectedRecordsException e) {
            List<RejectedRecord> rejectedRecords = e.rejectedRecords();
            log.error("Failed to write time series data. {} records were rejected", rejectedRecords.size());
            for (RejectedRecord rejectedRecord : rejectedRecords) {
                log.error("Rejected record: {}, reason: {}", 
                        rejectedRecord.recordIndex(), rejectedRecord.reason());
            }
            return false;
        } catch (Exception e) {
            log.error("Error writing to Timestream", e);
            return false;
        }
    }
    
    /**
     * Save a batch of time series data points.
     * 
     * @param timeSeriesDataList The list of time series data to save
     * @return True if the operation was successful
     */
    public boolean saveAll(List<TimeSeriesData> timeSeriesDataList) {
        if (!enabled || timestreamClient == null || timeSeriesDataList.isEmpty()) {
            log.debug("Timestream is disabled, client not initialized, or empty list. Skipping save operation.");
            return false;
        }
        
        try {
            List<Record> records = new ArrayList<>();
            for (TimeSeriesData timeSeriesData : timeSeriesDataList) {
                records.add(createRecord(timeSeriesData));
            }
            
            WriteRecordsRequest writeRecordsRequest = WriteRecordsRequest.builder()
                    .databaseName(database)
                    .tableName(table)
                    .records(records)
                    .build();
            
            WriteRecordsResponse response = timestreamClient.writeRecords(writeRecordsRequest);
            log.debug("Successfully wrote {} time series data points", timeSeriesDataList.size());
            return true;
        } catch (RejectedRecordsException e) {
            List<RejectedRecord> rejectedRecords = e.rejectedRecords();
            log.error("Failed to write time series data. {} records were rejected", rejectedRecords.size());
            for (RejectedRecord rejectedRecord : rejectedRecords) {
                log.error("Rejected record: {}, reason: {}", 
                        rejectedRecord.recordIndex(), rejectedRecord.reason());
            }
            return false;
        } catch (Exception e) {
            log.error("Error writing to Timestream", e);
            return false;
        }
    }
    
    /**
     * Create a Timestream record from time series data.
     * 
     * @param timeSeriesData The time series data
     * @return A Timestream record
     */
    private Record createRecord(TimeSeriesData timeSeriesData) {
        // Create dimensions
        List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(createDimension("metric_name", timeSeriesData.getMetricName()));
        dimensions.add(createDimension("service_id", timeSeriesData.getServiceId()));
        
        if (timeSeriesData.getResourceId() != null) {
            dimensions.add(createDimension("resource_id", timeSeriesData.getResourceId()));
        }
        
        dimensions.add(createDimension("environment", timeSeriesData.getEnvironment()));
        dimensions.add(createDimension("region", timeSeriesData.getRegion()));
        dimensions.add(createDimension("availability_zone", timeSeriesData.getAvailabilityZone()));
        dimensions.add(createDimension("unit", timeSeriesData.getUnit()));
        
        // Create measure value
        MeasureValue measureValue = MeasureValue.builder()
                .name("value")
                .value(String.valueOf(timeSeriesData.getValue()))
                .type(MeasureValueType.DOUBLE)
                .build();
        
        // Create record
        return Record.builder()
                .dimensions(dimensions)
                .measureName("metrics")
                .measureValue(measureValue.value())
                .measureValueType(measureValue.type())
                .time(String.valueOf(timeSeriesData.getTimestamp().toEpochMilli()))
                .timeUnit(software.amazon.awssdk.services.timestreamwrite.model.TimeUnit.MILLISECONDS)
                .build();
    }
    
    /**
     * Create a Timestream dimension.
     * 
     * @param name The dimension name
     * @param value The dimension value
     * @return A Timestream dimension
     */
    private Dimension createDimension(String name, String value) {
        return Dimension.builder()
                .name(name)
                .value(value)
                .build();
    }
}
