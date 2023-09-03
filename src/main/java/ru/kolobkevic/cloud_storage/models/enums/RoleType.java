package ru.kolobkevic.cloud_storage.models.enums;

public enum RoleType {
    USER, ADMIN;

    public String getRole() {
        return String.format("ROLE_%s", this);
    }
}
