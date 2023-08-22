package ru.kolobkevic.cloud_storage.entities.enums;

public enum RoleType {
    USER, ADMIN;

    public String getRole() {
        return String.format("ROLE_%s", this);
    }
}
