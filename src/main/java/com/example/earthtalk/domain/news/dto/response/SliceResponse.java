package com.example.earthtalk.domain.news.dto.response;

import java.util.List;
import org.springframework.data.domain.Slice;

public record SliceResponse<T>(
    List<T> content,
    boolean hasNext,
    boolean last,
    int numberOfElements,
    int size
) {
    public SliceResponse(Slice<T> slice) {
        this(slice.getContent(), slice.hasNext(), !slice.hasNext(), slice.getNumberOfElements(), slice.getSize());
    }
}