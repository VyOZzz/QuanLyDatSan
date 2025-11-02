package com.codewithvy.quanlydatsan.service;

import com.codewithvy.quanlydatsan.dto.AddressDTO;
import com.codewithvy.quanlydatsan.dto.PriceRuleRequest;
import com.codewithvy.quanlydatsan.dto.VenuesDTO;
import com.codewithvy.quanlydatsan.dto.VenuesRequest;
import com.codewithvy.quanlydatsan.entity.Address;
import com.codewithvy.quanlydatsan.entity.PriceRules;
import com.codewithvy.quanlydatsan.entity.User;
import com.codewithvy.quanlydatsan.entity.Venues;
import com.codewithvy.quanlydatsan.exception.ResourceNotFoundException;
import com.codewithvy.quanlydatsan.mapper.VenuesMapper;
import com.codewithvy.quanlydatsan.repository.AddressRepository;
import com.codewithvy.quanlydatsan.repository.PriceRuleRepository;
import com.codewithvy.quanlydatsan.repository.UserRepository;
import com.codewithvy.quanlydatsan.repository.VenuesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VenuesService {
    private static final Logger log = LoggerFactory.getLogger(VenuesService.class);

    private final VenuesRepository venuesRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final PriceRuleRepository priceRuleRepository;

    public VenuesService(VenuesRepository venuesRepository, AddressRepository addressRepository,
                         UserRepository userRepository, PriceRuleRepository priceRuleRepository) {
        this.venuesRepository = venuesRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.priceRuleRepository = priceRuleRepository;
    }

    public List<VenuesDTO> getAll() {
        return venuesRepository.findAll().stream().map(VenuesMapper::toDto).collect(Collectors.toList());
    }

    public VenuesDTO getById(Long id) {
        Venues v = venuesRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Venues not found with id=" + id));
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

    private String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Transactional
    public VenuesDTO create(VenuesRequest request) {
        log.info("Creating venue with request: {}", request);

        if (request.getAddress() == null) {
            log.error("Address is null in request");
            throw new IllegalArgumentException("Address is required");
        }

        log.info("Address received - province: {}, district: {}, detail: {}",
                request.getAddress().getProvinceOrCity(),
                request.getAddress().getDistrict(),
                request.getAddress().getDetailAddress());

        // Tạo Address mới từ AddressDTO
        Address address = createAddressFromDTO(request.getAddress());
        log.info("Address created with id: {}", address.getId());

        // Lấy user hiện tại làm owner
        User currentUser = getCurrentUser();
        log.info("Current user found: id={}, phone={}, roles={}",
                currentUser.getId(),
                currentUser.getPhone(),
                currentUser.getRoles());

        Venues v = new Venues();
        v.setName(request.getName());
        v.setDescription(request.getDescription());
        v.setPhoneNumber(request.getPhoneNumber());
        v.setEmail(request.getEmail());
        v.setAddress(address);
        v.setOwner(currentUser); // SET OWNER - BẮT BUỘC
        v.setNumberOfCourt(0); // Khởi tạo = 0, sẽ tự động tăng khi thêm court

        log.info("Saving venue: {}", v.getName());
        Venues saved = venuesRepository.save(v);
        log.info("Venue saved successfully with id: {}", saved.getId());

        return VenuesMapper.toDto(saved);
    }

    @Transactional
    public VenuesDTO update(Long id, VenuesRequest request) {
        Venues existing = venuesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venues not found with id=" + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            existing.setName(request.getName());
        }

        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }

        if (request.getPhoneNumber() != null) {
            existing.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getEmail() != null) {
            existing.setEmail(request.getEmail());
        }

        if (request.getAddress() != null) {
            // Tạo Address mới từ AddressDTO
            Address newAddress = createAddressFromDTO(request.getAddress());
            existing.setAddress(newAddress);
        }

        // Cập nhật price rules nếu có
        if (request.getPriceRules() != null && !request.getPriceRules().isEmpty()) {
            log.info("Updating price rules for venue id: {}", id);
            updatePriceRules(existing, request.getPriceRules());
        }

        return VenuesMapper.toDto(existing); // managed entity auto flushed
    }

    @Transactional
    public void delete(Long id) {
        if (!venuesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Venues not found with id=" + id);
        }
        venuesRepository.deleteById(id);
    }

    /**
     * Tạo Address entity từ AddressDTO
     */
    private Address createAddressFromDTO(AddressDTO dto) {
        log.info("Creating address from DTO");
        Address address = new Address();
        address.setDetailAddress(dto.getDetailAddress());
        address.setDistrict(dto.getDistrict());
        address.setProvinceOrCity(dto.getProvinceOrCity());

        log.info("Saving address to database");
        Address saved = addressRepository.save(address);
        log.info("Address saved with id: {}", saved.getId());
        return saved;
    }

    /**
     * Lấy thông tin user hiện tại đang đăng nhập
     */
    private User getCurrentUser() {
        String phone = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Getting current user with phone: {}", phone);
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Cập nhật price rules cho venue.
     * Xóa tất cả price rules cũ và tạo mới từ request.
     */
    private void updatePriceRules(Venues venue, List<PriceRuleRequest> priceRuleRequests) {
        // Xóa tất cả price rules cũ
        List<PriceRules> oldRules = priceRuleRepository.findByVenues(venue);
        if (!oldRules.isEmpty()) {
            log.info("Deleting {} old price rules", oldRules.size());
            priceRuleRepository.deleteAll(oldRules);
        }

        // Tạo price rules mới
        for (PriceRuleRequest request : priceRuleRequests) {
            PriceRules priceRule = new PriceRules();
            priceRule.setName(request.getName());
            priceRule.setStartTime(request.getStartTime());
            priceRule.setEndTime(request.getEndTime());
            priceRule.setPricePerHour(request.getPricePerHour());
            priceRule.setVenues(venue);
            priceRule.setActive(true);

            priceRuleRepository.save(priceRule);
            log.info("Created new price rule: {} for venue id: {}", request.getName(), venue.getId());
        }
    }
}
