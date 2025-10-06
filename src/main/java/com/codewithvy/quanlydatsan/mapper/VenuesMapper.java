package com.codewithvy.quanlydatsan.mapper;

import com.codewithvy.quanlydatsan.dto.AddressDTO;
import com.codewithvy.quanlydatsan.dto.VenuesDTO;
import com.codewithvy.quanlydatsan.entity.Address;
import com.codewithvy.quanlydatsan.entity.Venues;

public class VenuesMapper {

    public static VenuesDTO toDto(Venues v){
        if(v == null) return null;
        Address address = v.getAddress();
        AddressDTO addressDTO = null;
        if(address != null){
            addressDTO = AddressDTO.builder()
                    .id(address.getId())
                    .provinceOrCity(address.getProvinceOrCity())
                    .district(address.getDistrict())
                    .detailAddress(address.getDetailAddress())
                    .build();
        }
        Integer courtsCount = v.getCourts() != null ? v.getCourts().size() : 0;
        return VenuesDTO.builder()
                .id(v.getId())
                .name(v.getName())
                .numberOfCourt(v.getNumberOfCourt())
                .address(addressDTO)
                .courtsCount(courtsCount)
                .build();
    }
}

