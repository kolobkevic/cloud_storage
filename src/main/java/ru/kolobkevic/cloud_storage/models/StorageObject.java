package ru.kolobkevic.cloud_storage.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageObject {
    private String objectName;
    private String path;
    private boolean isDir;
}
