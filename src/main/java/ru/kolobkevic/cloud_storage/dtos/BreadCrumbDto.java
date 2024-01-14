package ru.kolobkevic.cloud_storage.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
@NoArgsConstructor
public class BreadCrumbDto {
    private List<String> breadCrumbs;
    private String breadCrumbName;

    public BreadCrumbDto(List<String> breadCrumbsList) {
        breadCrumbs = breadCrumbsList;
        breadCrumbName = breadCrumbsList.get(breadCrumbsList.size() - 1);
    }

    @Override
    public String toString() {
        return String.join("/", breadCrumbs) + "/";
    }
}
