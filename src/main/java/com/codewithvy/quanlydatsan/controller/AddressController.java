package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.dto.AddressDTO;
import com.codewithvy.quanlydatsan.dto.ApiResponse;
import com.codewithvy.quanlydatsan.entity.Address;
import com.codewithvy.quanlydatsan.exception.ResourceNotFoundException;
import com.codewithvy.quanlydatsan.repository.AddressRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    private final AddressRepository addressRepository;

    public AddressController(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    private AddressDTO toDto(Address a){
        if(a==null) return null;
        return AddressDTO.builder()
                .id(a.getId())
                .provinceOrCity(a.getProvinceOrCity())
                .district(a.getDistrict())
                .detailAddress(a.getDetailAddress())
                .build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getAll(){
        List<AddressDTO> list = addressRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list, "List addresses"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressDTO>> getById(@PathVariable Long id){
        Address a = addressRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        return ResponseEntity.ok(ApiResponse.ok(toDto(a)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressDTO>> create(@RequestBody AddressDTO req){
        Address a = new Address();
        a.setProvinceOrCity(req.getProvinceOrCity());
        a.setDistrict(req.getDistrict());
        a.setDetailAddress(req.getDetailAddress());
        Address saved = addressRepository.save(a);
        return ResponseEntity.ok(ApiResponse.ok(toDto(saved), "Created"));
    }
}

