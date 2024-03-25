package ru.kolobkevic.cloud_storage.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilesUploadDto {
    private List<MultipartFile> files;
    private String username;
    private String path;
}
