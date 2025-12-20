package com.codewithvy.quanlydatsan.service.impl;

import com.codewithvy.quanlydatsan.dto.LienLacDTO;
import com.codewithvy.quanlydatsan.dto.LienLacRequest;
import com.codewithvy.quanlydatsan.dto.LienLacUpdateRequest;
import com.codewithvy.quanlydatsan.entity.LienLac;
import com.codewithvy.quanlydatsan.entity.User;
import com.codewithvy.quanlydatsan.entity.Venues;
import com.codewithvy.quanlydatsan.exception.ResourceNotFoundException;
import com.codewithvy.quanlydatsan.exception.UnauthorizedException;
import com.codewithvy.quanlydatsan.mapper.LienLacMapper;
import com.codewithvy.quanlydatsan.repository.LienLacRepository;
import com.codewithvy.quanlydatsan.repository.UserRepository;
import com.codewithvy.quanlydatsan.repository.VenuesRepository;
import com.codewithvy.quanlydatsan.service.LienLacService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LienLacServiceImpl implements LienLacService {

    private final LienLacRepository lienLacRepository;
    private final UserRepository userRepository;
    private final VenuesRepository venuesRepository;

    @Override
    @Transactional
    public LienLacDTO createLienLac(LienLacRequest request, String userPhone) {
        LienLac lienLac = new LienLac();

        // Nếu user đã đăng nhập thì gắn user vào
        if (userPhone != null && !userPhone.isEmpty()) {
            User user = userRepository.findByPhone(userPhone)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            lienLac.setUser(user);
        }

        // Set thông tin liên hệ
        lienLac.setName(request.getName());
        lienLac.setEmail(request.getEmail());
        lienLac.setPhone(request.getPhone());
        lienLac.setSubject(request.getSubject());
        lienLac.setMessage(request.getMessage());

        // Nếu có venueId, kiểm tra và gắn venue
        if (request.getVenueId() != null) {
            Venues venue = venuesRepository.findById(request.getVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + request.getVenueId()));
            lienLac.setVenue(venue);
        }

        LienLac savedLienLac = lienLacRepository.save(lienLac);
        return LienLacMapper.toDto(savedLienLac);
    }

    @Override
    public List<LienLacDTO> getAllLienLac() {
        List<LienLac> lienLacs = lienLacRepository.findAll();
        return lienLacs.stream()
                .map(LienLacMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LienLacDTO> getMyLienLac(String userPhone) {
        User user = userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<LienLac> lienLacs = lienLacRepository.findByUserId(user.getId());
        return lienLacs.stream()
                .map(LienLacMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LienLacDTO> getLienLacByVenue(Long venueId, String userPhone) {
        // Kiểm tra venue tồn tại
        Venues venue = venuesRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + venueId));

        // Kiểm tra quyền: chỉ owner của venue mới được xem
        User user = userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!venue.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only view contacts for your own venues");
        }

        List<LienLac> lienLacs = lienLacRepository.findByVenueId(venueId);
        return lienLacs.stream()
                .map(LienLacMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LienLacDTO> getLienLacByOwner(String userPhone) {
        User user = userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<LienLac> lienLacs = lienLacRepository.findByVenueOwnerId(user.getId());
        return lienLacs.stream()
                .map(LienLacMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public LienLacDTO getLienLacById(Long id, String userPhone) {
        LienLac lienLac = lienLacRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));

        User user = userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Kiểm tra quyền: user tạo liên lạc hoặc owner của venue liên quan mới được xem
        boolean isOwner = lienLac.getUser() != null && lienLac.getUser().getId().equals(user.getId());
        boolean isVenueOwner = lienLac.getVenue() != null && 
                lienLac.getVenue().getOwner().getId().equals(user.getId());

        if (!isOwner && !isVenueOwner) {
            throw new UnauthorizedException("You don't have permission to view this contact");
        }

        return LienLacMapper.toDto(lienLac);
    }

    @Override
    @Transactional
    public LienLacDTO updateLienLac(Long id, LienLacUpdateRequest request, String userPhone) {
        LienLac lienLac = lienLacRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));

        User user = userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Chỉ owner của venue liên quan mới được cập nhật
        if (lienLac.getVenue() == null || !lienLac.getVenue().getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only update contacts for your own venues");
        }

        // Cập nhật trạng thái và phản hồi
        lienLac.setStatus(request.getStatus());
        if (request.getResponse() != null) {
            lienLac.setResponse(request.getResponse());
        }
        lienLac.setHandler(user);

        LienLac updatedLienLac = lienLacRepository.save(lienLac);
        return LienLacMapper.toDto(updatedLienLac);
    }

    @Override
    @Transactional
    public void deleteLienLac(Long id, String userPhone) {
        LienLac lienLac = lienLacRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));

        User user = userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Chỉ owner của venue liên quan mới được xóa
        if (lienLac.getVenue() == null || !lienLac.getVenue().getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only delete contacts for your own venues");
        }

        lienLacRepository.delete(lienLac);
    }
}
