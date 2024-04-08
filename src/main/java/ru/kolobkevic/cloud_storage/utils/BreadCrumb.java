package ru.kolobkevic.cloud_storage.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BreadCrumb {
    private List<String> breadCrumbs;
    private String breadCrumbName;

    public BreadCrumb(List<String> breadCrumbsList) {
        breadCrumbs = breadCrumbsList;
        breadCrumbName = breadCrumbsList.get(breadCrumbsList.size() - 1);
    }

    @Override
    public String toString() {
        return String.join("/", breadCrumbs) + "/";
    }
}
