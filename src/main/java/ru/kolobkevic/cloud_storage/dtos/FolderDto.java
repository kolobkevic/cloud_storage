package ru.kolobkevic.cloud_storage.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FolderDto {
    private String objectName;
    private String path;
    private String username;
}
