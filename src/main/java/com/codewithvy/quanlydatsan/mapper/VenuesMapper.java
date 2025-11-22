package com.codewithvy.quanlydatsan.mapper;

import com.codewithvy.quanlydatsan.dto.AddressDTO;
import com.codewithvy.quanlydatsan.dto.VenuesDTO;
import com.codewithvy.quanlydatsan.entity.Address;
import com.codewithvy.quanlydatsan.entity.Venues;
import com.codewithvy.quanlydatsan.repository.ReviewRepository;
import org.springframework.stereotype.Component;

@Component
public class VenuesMapper {

    private static ReviewRepository reviewRepository;

    // Constructor injection
    public VenuesMapper(ReviewRepository reviewRepository) {
        VenuesMapper.reviewRepository = reviewRepository;
    }

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
        // Dùng numberOfCourt thay vì getCourts().size() để tránh lazy loading
        Integer courtsCount = v.getNumberOfCourt();

        // Tính toán động averageRating và totalReviews từ bảng review
        Double averageRating = 0.0;
        Integer totalReviews = 0;
        if (reviewRepository != null) {
            Double avgRating = reviewRepository.calculateAverageRating(v.getId());
            averageRating = avgRating != null ? avgRating : 0.0;
            totalReviews = (int) reviewRepository.countByVenuesId(v.getId());
        }

        return VenuesDTO.builder()
                .id(v.getId())
                .name(v.getName())
                .description(v.getDescription()) // Map trường description
                .numberOfCourt(v.getNumberOfCourt())
                .address(addressDTO)
                .courtsCount(courtsCount)
                .pricePerHour(v.getPricePerHour())
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .openingTime(v.getOpeningTime())
                .closingTime(v.getClosingTime())
                .images(v.getImages()) // Map danh sách ảnh
                .phoneNumber(v.getPhoneNumber()) // Map số điện thoại của venue
                .email(v.getEmail()) // Map email của venue
                .ownerPhoneNumber(v.getOwner() != null ? v.getOwner().getPhone() : null) // Map số điện thoại chủ sân
                .build();
    }
}
