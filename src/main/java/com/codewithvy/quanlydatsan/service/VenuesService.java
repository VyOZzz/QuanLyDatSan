package com.codewithvy.quanlydatsan.service;

import com.codewithvy.quanlydatsan.dto.VenuesDTO;
import com.codewithvy.quanlydatsan.dto.VenuesRequest;
import com.codewithvy.quanlydatsan.entity.Address;
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

    public VenuesDTO getById(Long id){
        Venues v = venuesRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Venues not found with id="+id));
        return VenuesMapper.toDto(v);
    }

    /**
     * Tìm kiếm venues theo tên và/hoặc địa chỉ (province/district/detail). Nếu tham số null/blank sẽ bỏ qua điều kiện.
     */
    public List<VenuesDTO> search(String name, String province, String district, String detail) {
        String n = normalize(name);
        String p = normalize(province);
        String d = normalize(district);
        String de = normalize(detail);
        return venuesRepository.search(n, p, d, de)
                .stream().map(VenuesMapper::toDto).collect(Collectors.toList());
    }

    private String normalize(String s){
        if(s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
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
