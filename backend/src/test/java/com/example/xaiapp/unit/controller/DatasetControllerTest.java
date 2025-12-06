/**
 * Unit tests for DatasetController
 * Tests controller endpoints with mocked services
 */
package com.example.xaiapp.unit.controller;

import com.example.xaiapp.controller.DatasetController;
import com.example.xaiapp.dto.ApiResponse;
import com.example.xaiapp.dto.DatasetDto;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.service.DatasetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatasetControllerTest {

    @Mock
    private DatasetService datasetService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DatasetController datasetController;

    private User testUser;
    private DatasetDto testDatasetDto;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testDatasetDto = new DatasetDto();
        testDatasetDto.setId(1L);
        testDatasetDto.setName("test-dataset.csv");
        testDatasetDto.setFileName("test-dataset.csv");

        testFile = new MockMultipartFile(
            "file",
            "test-dataset.csv",
            "text/csv",
            "col1,col2\nval1,val2".getBytes()
        );
    }

    /**
     * Test successful dataset upload
     * Verifies that a valid file upload returns 200 OK with success response
     */
    @Test
    void testUploadDataset_Success() throws IOException {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(datasetService.storeFile(any(MultipartFile.class), eq(1L)))
            .thenReturn(testDatasetDto);

        // When
        ResponseEntity<?> response = datasetController.uploadDataset(testFile, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertTrue(apiResponse.isSuccess());
        verify(datasetService, times(1)).storeFile(testFile, 1L);
    }

    /**
     * Test dataset upload with IO exception
     * Verifies that IO errors are handled and return 400 Bad Request
     */
    @Test
    void testUploadDataset_IOException() throws IOException {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(datasetService.storeFile(any(MultipartFile.class), eq(1L)))
            .thenThrow(new IOException("File read error"));

        // When
        ResponseEntity<?> response = datasetController.uploadDataset(testFile, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Failed to upload dataset"));
    }

    /**
     * Test dataset upload with invalid file format
     * Verifies that invalid file format errors return 400 Bad Request
     */
    @Test
    void testUploadDataset_IllegalArgumentException() throws IOException {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(datasetService.storeFile(any(MultipartFile.class), eq(1L)))
            .thenThrow(new IllegalArgumentException("Invalid file format"));

        // When
        ResponseEntity<?> response = datasetController.uploadDataset(testFile, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Invalid file format", apiResponse.getMessage());
    }

    /**
     * Test retrieving all user datasets
     * Verifies that user's datasets are returned successfully
     */
    @Test
    void testGetUserDatasets_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        List<DatasetDto> datasets = Arrays.asList(testDatasetDto);
        when(datasetService.listUserDatasets(1L)).thenReturn(datasets);

        // When
        ResponseEntity<List<DatasetDto>> response = 
            datasetController.getUserDatasets(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testDatasetDto.getId(), response.getBody().get(0).getId());
        verify(datasetService, times(1)).listUserDatasets(1L);
    }

    /**
     * Test retrieving datasets when user has none
     * Verifies that empty list is returned for users with no datasets
     */
    @Test
    void testGetUserDatasets_EmptyList() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(datasetService.listUserDatasets(1L)).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<DatasetDto>> response = 
            datasetController.getUserDatasets(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    /**
     * Test retrieving a specific dataset by ID
     * Verifies that existing dataset is returned with 200 OK
     */
    @Test
    void testGetDataset_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(datasetService.getDataset(1L, 1L)).thenReturn(Optional.of(testDatasetDto));

        // When
        ResponseEntity<?> response = datasetController.getDataset(1L, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testDatasetDto, response.getBody());
        verify(datasetService, times(1)).getDataset(1L, 1L);
    }

    /**
     * Test retrieving non-existent dataset
     * Verifies that missing dataset returns 404 Not Found
     */
    @Test
    void testGetDataset_NotFound() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(datasetService.getDataset(1L, 1L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = datasetController.getDataset(1L, authentication);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(datasetService, times(1)).getDataset(1L, 1L);
    }

    /**
     * Test retrieving dataset with exception
     * Verifies that exceptions during retrieval return 400 Bad Request
     */
    @Test
    void testGetDataset_Exception() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(datasetService.getDataset(1L, 1L))
            .thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<?> response = datasetController.getDataset(1L, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Failed to retrieve dataset"));
    }

    /**
     * Test successful dataset deletion
     * Verifies that dataset deletion returns 200 OK with success message
     */
    @Test
    void testDeleteDataset_Success() throws IOException {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        doNothing().when(datasetService).deleteDataset(1L, 1L);

        // When
        ResponseEntity<ApiResponse> response = 
            datasetController.deleteDataset(1L, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Dataset deleted successfully", response.getBody().getMessage());
        verify(datasetService, times(1)).deleteDataset(1L, 1L);
    }

    /**
     * Test dataset deletion with IO exception
     * Verifies that file deletion errors return 400 Bad Request
     */
    @Test
    void testDeleteDataset_IOException() throws IOException {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        doThrow(new IOException("File not found")).when(datasetService).deleteDataset(1L, 1L);

        // When
        ResponseEntity<ApiResponse> response = 
            datasetController.deleteDataset(1L, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Failed to delete dataset"));
    }

    /**
     * Test dataset deletion with runtime exception
     * Verifies that runtime errors during deletion return 400 Bad Request
     */
    @Test
    void testDeleteDataset_RuntimeException() throws IOException {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        doThrow(new RuntimeException("Dataset not found"))
            .when(datasetService).deleteDataset(1L, 1L);

        // When
        ResponseEntity<ApiResponse> response = 
            datasetController.deleteDataset(1L, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Dataset not found", response.getBody().getMessage());
    }
}

