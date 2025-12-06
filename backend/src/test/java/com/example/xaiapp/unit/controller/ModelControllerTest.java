/**
 * Unit tests for ModelController
 * Tests controller endpoints with mocked services
 */
package com.example.xaiapp.unit.controller;

import com.example.xaiapp.controller.ModelController;
import com.example.xaiapp.dto.ApiResponse;
import com.example.xaiapp.dto.ExplanationResponse;
import com.example.xaiapp.dto.PredictionResponse;
import com.example.xaiapp.dto.TrainRequestDto;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.exception.DatasetParsingException;
import com.example.xaiapp.exception.ModelTrainingException;
import com.example.xaiapp.service.ModelService;
import com.example.xaiapp.service.XaiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelControllerTest {

    @Mock
    private ModelService modelService;

    @Mock
    private XaiService xaiService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ModelController modelController;

    private User testUser;
    private MLModel testModel;
    private TrainRequestDto trainRequest;
    private PredictionResponse predictionResponse;
    private ExplanationResponse explanationResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testModel = new MLModel();
        testModel.setId(1L);
        testModel.setName("test-model");
        testModel.setModelType("CLASSIFICATION");

        trainRequest = new TrainRequestDto();
        trainRequest.setDatasetId(1L);
        trainRequest.setModelName("test-model");
        trainRequest.setTargetVariable("target");
        trainRequest.setFeatureNames(Arrays.asList("feature1", "feature2"));
        trainRequest.setModelType("CLASSIFICATION");

        predictionResponse = new PredictionResponse();
        predictionResponse.setPrediction("class1");
        predictionResponse.setConfidence(0.95);

        explanationResponse = new ExplanationResponse();
        explanationResponse.setPrediction("class1");
        explanationResponse.setConfidence(0.95);
    }

    /**
     * Test successful model training
     * Verifies that valid training request returns 200 OK with trained model
     */
    @Test
    void testTrainModel_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(modelService.trainModel(any(TrainRequestDto.class), eq(1L)))
            .thenReturn(testModel);

        // When
        ResponseEntity<?> response = modelController.trainModel(trainRequest, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertTrue(apiResponse.isSuccess());
        assertEquals("Model trained successfully", apiResponse.getMessage());
        verify(modelService, times(1)).trainModel(trainRequest, 1L);
    }

    /**
     * Test model training with dataset parsing error
     * Verifies that invalid dataset format returns 400 Bad Request
     */
    @Test
    void testTrainModel_DatasetParsingException() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(modelService.trainModel(any(TrainRequestDto.class), eq(1L)))
            .thenThrow(new DatasetParsingException("Invalid dataset format"));

        // When
        ResponseEntity<?> response = modelController.trainModel(trainRequest, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid dataset"));
    }

    /**
     * Test model training failure
     * Verifies that training errors return 500 Internal Server Error
     */
    @Test
    void testTrainModel_ModelTrainingException() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(modelService.trainModel(any(TrainRequestDto.class), eq(1L)))
            .thenThrow(new ModelTrainingException("Training failed"));

        // When
        ResponseEntity<?> response = modelController.trainModel(trainRequest, authentication);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Training failed"));
    }

    /**
     * Test model training with invalid parameters
     * Verifies that invalid parameters return 400 Bad Request
     */
    @Test
    void testTrainModel_IllegalArgumentException() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(modelService.trainModel(any(TrainRequestDto.class), eq(1L)))
            .thenThrow(new IllegalArgumentException("Invalid parameters"));

        // When
        ResponseEntity<?> response = modelController.trainModel(trainRequest, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid parameters"));
    }

    /**
     * Test model training with unexpected error
     * Verifies that unexpected errors return 500 Internal Server Error
     */
    @Test
    void testTrainModel_UnexpectedException() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(modelService.trainModel(any(TrainRequestDto.class), eq(1L)))
            .thenThrow(new RuntimeException("Unexpected error"));

        // When
        ResponseEntity<?> response = modelController.trainModel(trainRequest, authentication);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Unexpected error"));
    }

    /**
     * Test retrieving all user models
     * Verifies that user's models are returned successfully
     */
    @Test
    void testGetUserModels_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        List<MLModel> models = Arrays.asList(testModel);
        when(modelService.getUserModels(1L)).thenReturn(models);

        // When
        ResponseEntity<List<MLModel>> response = 
            modelController.getUserModels(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testModel.getId(), response.getBody().get(0).getId());
        verify(modelService, times(1)).getUserModels(1L);
    }

    /**
     * Test retrieving models when user has none
     * Verifies that empty list is returned for users with no models
     */
    @Test
    void testGetUserModels_EmptyList() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(modelService.getUserModels(1L)).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<MLModel>> response = 
            modelController.getUserModels(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    /**
     * Test retrieving a specific model by ID
     * Verifies that existing model is returned with 200 OK
     */
    @Test
    void testGetModel_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(modelService.getModel(1L, 1L)).thenReturn(testModel);

        // When
        ResponseEntity<?> response = modelController.getModel(1L, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testModel, response.getBody());
        verify(modelService, times(1)).getModel(1L, 1L);
    }

    /**
     * Test retrieving non-existent model
     * Verifies that missing model returns 400 Bad Request
     */
    @Test
    void testGetModel_RuntimeException() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(modelService.getModel(1L, 1L))
            .thenThrow(new RuntimeException("Model not found"));

        // When
        ResponseEntity<?> response = modelController.getModel(1L, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Model not found", apiResponse.getMessage());
    }

    /**
     * Test successful prediction
     * Verifies that valid prediction request returns 200 OK with prediction result
     */
    @Test
    void testPredict_Success() {
        // Given
        Map<String, String> inputData = new HashMap<>();
        inputData.put("feature1", "value1");
        inputData.put("feature2", "value2");
        
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(xaiService.predict(1L, inputData, 1L)).thenReturn(predictionResponse);

        // When
        ResponseEntity<?> response = 
            modelController.predict(1L, inputData, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(predictionResponse, response.getBody());
        verify(xaiService, times(1)).predict(1L, inputData, 1L);
    }

    /**
     * Test prediction with error
     * Verifies that prediction errors return 400 Bad Request
     */
    @Test
    void testPredict_RuntimeException() {
        // Given
        Map<String, String> inputData = new HashMap<>();
        inputData.put("feature1", "value1");
        
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(xaiService.predict(1L, inputData, 1L))
            .thenThrow(new RuntimeException("Model not found"));

        // When
        ResponseEntity<?> response = 
            modelController.predict(1L, inputData, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Failed to make prediction"));
    }

    /**
     * Test successful explanation generation
     * Verifies that valid explanation request returns 200 OK with explanation
     */
    @Test
    void testExplain_Success() {
        // Given
        Map<String, String> inputData = new HashMap<>();
        inputData.put("feature1", "value1");
        inputData.put("feature2", "value2");
        
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(xaiService.explain(1L, inputData, 1L)).thenReturn(explanationResponse);

        // When
        ResponseEntity<?> response = 
            modelController.explain(1L, inputData, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(explanationResponse, response.getBody());
        verify(xaiService, times(1)).explain(1L, inputData, 1L);
    }

    /**
     * Test explanation generation with error
     * Verifies that explanation errors return 400 Bad Request
     */
    @Test
    void testExplain_RuntimeException() {
        // Given
        Map<String, String> inputData = new HashMap<>();
        inputData.put("feature1", "value1");
        
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(xaiService.explain(1L, inputData, 1L))
            .thenThrow(new RuntimeException("Model not found"));

        // When
        ResponseEntity<?> response = 
            modelController.explain(1L, inputData, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Failed to generate explanation"));
    }

    /**
     * Test successful model deletion
     * Verifies that model deletion returns 200 OK with success message
     */
    @Test
    void testDeleteModel_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        doNothing().when(modelService).deleteModel(1L, 1L);

        // When
        ResponseEntity<ApiResponse<String>> response = 
            modelController.deleteModel(1L, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Model deleted successfully", response.getBody().getMessage());
        verify(modelService, times(1)).deleteModel(1L, 1L);
    }

    /**
     * Test model deletion with error
     * Verifies that deletion errors return 400 Bad Request
     */
    @Test
    void testDeleteModel_Exception() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        doThrow(new RuntimeException("Model not found"))
            .when(modelService).deleteModel(1L, 1L);

        // When
        ResponseEntity<ApiResponse<String>> response = 
            modelController.deleteModel(1L, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Failed to delete model"));
    }
}

