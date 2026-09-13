package com.dat.erp.data;

import com.dat.erp.utils.ErrorUtils;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NamedResourceData {

    private final String name;
    private final String description;
    private final String type;

    public NamedResourceData(
            String name,
            String description,
            String type) {

        this.name = ErrorUtils.requireNotBlank(name, "Name is required");
        this.description = description == null ? null : ErrorUtils.requireNotBlank(description, "Description must not be blank");
        this.type = type == null ? null : ErrorUtils.requireNotBlank(type, "Type must not be blank");
    }
}
