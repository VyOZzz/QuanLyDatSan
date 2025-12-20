package com.codewithvy.quanlydatsan.mapper;

import com.codewithvy.quanlydatsan.dto.LienLacDTO;
import com.codewithvy.quanlydatsan.entity.LienLac;
import org.springframework.stereotype.Component;

@Component
public class LienLacMapper {

    public static LienLacDTO toDto(LienLac lienLac) {
        if (lienLac == null) return null;

        return LienLacDTO.builder()
                .id(lienLac.getId())
                .userId(lienLac.getUser() != null ? lienLac.getUser().getId() : null)
                .userFullname(lienLac.getUser() != null ? lienLac.getUser().getFullname() : null)
                .name(lienLac.getName())
                .email(lienLac.getEmail())
                .phone(lienLac.getPhone())
                .subject(lienLac.getSubject())
                .message(lienLac.getMessage())
                .status(lienLac.getStatus())
                .venueId(lienLac.getVenue() != null ? lienLac.getVenue().getId() : null)
                .venueName(lienLac.getVenue() != null ? lienLac.getVenue().getName() : null)
                .handlerId(lienLac.getHandler() != null ? lienLac.getHandler().getId() : null)
                .handlerName(lienLac.getHandler() != null ? lienLac.getHandler().getFullname() : null)
                .response(lienLac.getResponse())
                .createdAt(lienLac.getCreatedAt())
                .updatedAt(lienLac.getUpdatedAt())
                .build();
    }
}
