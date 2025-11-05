package com.codewithvy.quanlydatsan.service;

import com.codewithvy.quanlydatsan.dto.AddressDTO;
import com.codewithvy.quanlydatsan.dto.VenuesDTO;
import com.codewithvy.quanlydatsan.dto.VenuesRequest;
import com.codewithvy.quanlydatsan.entity.Address;
import com.codewithvy.quanlydatsan.entity.Court;
import com.codewithvy.quanlydatsan.entity.User;
import com.codewithvy.quanlydatsan.entity.Venues;
import com.codewithvy.quanlydatsan.exception.ResourceNotFoundException;
import com.codewithvy.quanlydatsan.mapper.VenuesMapper;
import com.codewithvy.quanlydatsan.repository.AddressRepository;
import com.codewithvy.quanlydatsan.repository.CourtRepository;
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
    private final CourtRepository courtRepository;

    public VenuesService(VenuesRepository venuesRepository, AddressRepository addressRepository,
                         UserRepository userRepository, CourtRepository courtRepository) {
        this.venuesRepository = venuesRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.courtRepository = courtRepository;
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

        if (request.getPricePerHour() == null) {
            log.error("PricePerHour is null in request");
            throw new IllegalArgumentException("Price per hour is required");
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
        v.setPricePerHour(request.getPricePerHour()); // SET GIÁ
        v.setOpeningTime(request.getOpeningTime()); // SET THỜI GIAN MỞ CỬA
        v.setClosingTime(request.getClosingTime()); // SET THỜI GIAN ĐÓNG CỬA

        // SET DANH SÁCH ẢNH
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            v.setImages(request.getImages());
            log.info("Setting {} images for venue", request.getImages().size());
        }

        log.info("Saving venue: {} with price: {}", v.getName(), v.getPricePerHour());
        Venues saved = venuesRepository.save(v);
        log.info("Venue saved successfully with id: {}", saved.getId());

        // TỰ ĐỘNG TẠO COURTS nếu có numberOfCourt trong request
        if (request.getNumberOfCourt() != null && request.getNumberOfCourt() > 0) {
            log.info("Auto-creating {} courts for venue id: {}", request.getNumberOfCourt(), saved.getId());
            createCourtsForVenue(saved, request.getNumberOfCourt());
            saved.setNumberOfCourt(request.getNumberOfCourt());
            venuesRepository.save(saved);
        }

        return VenuesMapper.toDto(saved);
    }

    @Transactional
    public VenuesDTO update(Long id, VenuesRequest request) {
        Venues existing = venuesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venues not found with id=" + id));

        // Kiểm tra quyền sở hữu - chỉ owner của venue mới được phép sửa
        User currentUser = getCurrentUser();
        if (!existing.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền chỉnh sửa venue này");
        }

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

        // Cập nhật giá nếu có
        if (request.getPricePerHour() != null) {
            log.info("Updating price for venue id: {} from {} to {}", id, existing.getPricePerHour(), request.getPricePerHour());
            existing.setPricePerHour(request.getPricePerHour());
        }

        // Cập nhật thời gian hoạt động nếu có
        if (request.getOpeningTime() != null) {
            existing.setOpeningTime(request.getOpeningTime());
        }

        if (request.getClosingTime() != null) {
            existing.setClosingTime(request.getClosingTime());
        }

        // Cập nhật danh sách ảnh nếu có
        if (request.getImages() != null) {
            existing.getImages().clear(); // Xóa ảnh cũ
            if (!request.getImages().isEmpty()) {
                existing.getImages().addAll(request.getImages()); // Thêm ảnh mới
                log.info("Updated {} images for venue id: {}", request.getImages().size(), id);
            }
        }

        // Cập nhật số lượng sân nếu có
        if (request.getNumberOfCourt() != null && request.getNumberOfCourt() > 0) {
            int currentNumberOfCourts = existing.getNumberOfCourt();
            int newNumberOfCourts = request.getNumberOfCourt();

            log.info("Updating numberOfCourt for venue id: {} from {} to {}", id, currentNumberOfCourts, newNumberOfCourts);

            if (newNumberOfCourts > currentNumberOfCourts) {
                // Tăng số sân - tạo thêm courts mới
                int courtsToAdd = newNumberOfCourts - currentNumberOfCourts;
                log.info("Adding {} new courts to venue id: {}", courtsToAdd, id);

                for (int i = currentNumberOfCourts + 1; i <= newNumberOfCourts; i++) {
                    Court court = new Court();
                    court.setDescription("Sân số " + i);
                    court.setVenues(existing);
                    courtRepository.save(court);
                    log.info("Created court {} for venue id: {}", i, id);
                }
            } else if (newNumberOfCourts < currentNumberOfCourts) {
                // Giảm số sân - xóa các courts thừa (xóa từ cuối)
                int courtsToRemove = currentNumberOfCourts - newNumberOfCourts;
                log.info("Removing {} courts from venue id: {}", courtsToRemove, id);

                List<Court> courts = courtRepository.findAll().stream()
                        .filter(court -> court.getVenues().getId().equals(id))
                        .sorted((c1, c2) -> c2.getId().compareTo(c1.getId())) // Sort descending by ID
                        .limit(courtsToRemove)
                        .collect(Collectors.toList());

                for (Court court : courts) {
                    log.info("Deleting court id: {} from venue id: {}", court.getId(), id);
                    courtRepository.delete(court);
                }
            }

            existing.setNumberOfCourt(newNumberOfCourts);
        }

        return VenuesMapper.toDto(existing); // managed entity auto flushed
    }

    @Transactional
    public void delete(Long id) {
        Venues existing = venuesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venues not found with id=" + id));

        // Kiểm tra quyền sở hữu - chỉ owner của venue mới được phép xóa
        User currentUser = getCurrentUser();
        if (!existing.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền xóa venue này");
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
     * Tự động tạo courts cho venue khi khởi tạo
     */
    private void createCourtsForVenue(Venues venue, int numberOfCourts) {
        for (int i = 1; i <= numberOfCourts; i++) {
            Court court = new Court();
            court.setDescription("Sân số " + i);
            court.setVenues(venue);
            courtRepository.save(court);
            log.info("Created court {} for venue id: {}", i, venue.getId());
        }
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
     * Lấy danh sách venues thuộc sở hữu của user hiện tại
     */
    public List<VenuesDTO> getMyVenues() {
        User currentUser = getCurrentUser();
        log.info("Getting venues for owner id: {}", currentUser.getId());
        return venuesRepository.findByOwnerId(currentUser.getId())
                .stream()
                .map(VenuesMapper::toDto)
                .collect(Collectors.toList());
    }
}
