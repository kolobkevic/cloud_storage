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
public class StorageObjRenameDto extends StorageObjDto {
    @NotEmpty
    @Pattern(regexp = "^[^\\\\\"$%&#'^*!?]*$", message = "Недопустимый символ в названии")
    private String newPath;
}
