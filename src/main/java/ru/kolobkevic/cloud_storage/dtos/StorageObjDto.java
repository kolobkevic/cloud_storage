package ru.kolobkevic.cloud_storage.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StorageObjDto {
    @NotEmpty
    @Pattern(regexp = "^[^\\\\\"$%&#'^*!?]*$", message = "Недопустимый символ в названии")
    private String objectName;
    private String path;
    private String username;
}
