package com.mycrewsoft.common.util;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DtoMapper {

    private final ObjectMapper objectMapper;

    public <T> T toDto(Object source, Class<T> targetType) {
        if (source == null) {
            return null;
        }

        return objectMapper.convertValue(source, targetType);
    }

    public <T> List<T> toDtoList(List<?> sourceList, Class<T> targetType) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }

        return sourceList.stream()
                .map(source -> toDto(source, targetType))
                .toList();
    }
}