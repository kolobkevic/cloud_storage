package ru.kolobkevic.cloud_storage.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDto {
    private MultipartFile file;
    private String username;
}
