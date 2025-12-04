/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-09-04 16:06:11
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:38:58
 */
package com.example.xaiapp.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainRequestDto {
    
    @NotNull(message = "Dataset ID is required")
    private Long datasetId;
    
    @NotBlank(message = "Model name is required")
    private String modelName;
    
    @NotBlank(message = "Target variable is required")
    private String targetVariable;
    
    @NotEmpty(message = "Feature names are required")
    private List<String> featureNames;
    
    @NotBlank(message = "Model type is required")
    private String modelType; // "CLASSIFICATION" or "REGRESSION"
    
    // Manual getters and setters (Lombok not generating them)
    public Long getDatasetId() { return datasetId; }
    public void setDatasetId(Long datasetId) { this.datasetId = datasetId; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getTargetVariable() { return targetVariable; }
    public void setTargetVariable(String targetVariable) { this.targetVariable = targetVariable; }
    public List<String> getFeatureNames() { return featureNames; }
    public void setFeatureNames(List<String> featureNames) { this.featureNames = featureNames; }
    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }
}
