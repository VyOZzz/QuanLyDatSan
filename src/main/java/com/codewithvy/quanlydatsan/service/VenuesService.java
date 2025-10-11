package com.codewithvy.quanlydatsan.service;

import com.codewithvy.quanlydatsan.dto.*;
import com.codewithvy.quanlydatsan.entity.Address;
import com.codewithvy.quanlydatsan.entity.Court;
import com.codewithvy.quanlydatsan.entity.Venues;
import com.codewithvy.quanlydatsan.exception.ResourceNotFoundException;
import com.codewithvy.quanlydatsan.mapper.VenuesMapper;
import com.codewithvy.quanlydatsan.repository.AddressRepository;
import com.codewithvy.quanlydatsan.repository.VenuesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VenuesService {
    private final VenuesRepository venuesRepository;
    private final AddressRepository addressRepository;

    public VenuesService(VenuesRepository venuesRepository, AddressRepository addressRepository) {
        this.venuesRepository = venuesRepository;
        this.addressRepository = addressRepository;
    }

    public List<VenuesDTO> getAll(){
        return venuesRepository.findAll().stream().map(VenuesMapper::toDto).collect(Collectors.toList());
    }

    // New: filter by province/city if provided
    public List<VenuesDTO> getAll(String provinceOrCity){
        List<Venues> list = (provinceOrCity == null || provinceOrCity.isBlank())
                ? venuesRepository.findAll()
                : venuesRepository.searchByProvinceOrCity(provinceOrCity);
        return list.stream().map(VenuesMapper::toDto).collect(Collectors.toList());
    }

    // Change: return detail DTO including courts
    public VenuesDetailDTO getById(Long id){
        Venues v = venuesRepository.findWithCourtsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venues not found with id="+id));
        Address address = v.getAddress();
        AddressDTO addressDTO = address == null ? null : AddressDTO.builder()
                .id(address.getId())
                .provinceOrCity(address.getProvinceOrCity())
                .district(address.getDistrict())
                .detailAddress(address.getDetailAddress())
                .build();
        List<CourtDTO> courts = v.getCourts() == null ? java.util.List.of() : v.getCourts().stream()
                .map(this::toCourtDto)
                .collect(Collectors.toList());
        return VenuesDetailDTO.builder()
                .id(v.getId())
                .name(v.getName())
                .numberOfCourt(v.getNumberOfCourt())
                .address(addressDTO)
                .courts(courts)
                .build();
    }

    private CourtDTO toCourtDto(Court c){
        return new CourtDTO(c.getId(), c.isBooked());
    }

    @Transactional
    public VenuesDTO create(VenuesRequest request){
        if(request.getAddressId() == null){
            throw new ResourceNotFoundException("addressId is required");
        }
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id="+request.getAddressId()));
        Venues v = new Venues();
        v.setName(request.getName());
        v.setNumberOfCourt(request.getNumberOfCourt() == null?0: request.getNumberOfCourt());
        v.setAddress(address);
        Venues saved = venuesRepository.save(v);
        return VenuesMapper.toDto(saved);
    }

    @Transactional
    public VenuesDTO update(Long id, VenuesRequest request){
        Venues existing = venuesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venues not found with id="+id));
        if(request.getName()!=null) existing.setName(request.getName());
        if(request.getNumberOfCourt()!=null) existing.setNumberOfCourt(request.getNumberOfCourt());
        if(request.getAddressId()!=null){
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found with id="+request.getAddressId()));
            existing.setAddress(address);
        }
        return VenuesMapper.toDto(existing); // managed entity auto flushed
    }

    @Transactional
    public void delete(Long id){
        if(!venuesRepository.existsById(id)){
            throw new ResourceNotFoundException("Venues not found with id="+id);
        }
        venuesRepository.deleteById(id);
    }
}
