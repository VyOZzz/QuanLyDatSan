package com.codewithvy.quanlydatsan.service;

import com.codewithvy.quanlydatsan.dto.LienLacDTO;
import com.codewithvy.quanlydatsan.dto.LienLacRequest;
import com.codewithvy.quanlydatsan.dto.LienLacUpdateRequest;

import java.util.List;

public interface LienLacService {

    /**
     * Tạo liên lạc mới (có thể từ user đã đăng nhập hoặc khách)
     */
    LienLacDTO createLienLac(LienLacRequest request, String userPhone);

    /**
     * Lấy tất cả liên lạc (cho admin/owner)
     */
    List<LienLacDTO> getAllLienLac();

    /**
     * Lấy liên lạc của user hiện tại
     */
    List<LienLacDTO> getMyLienLac(String userPhone);

    /**
     * Lấy liên lạc theo venue (cho owner của venue đó)
     */
    List<LienLacDTO> getLienLacByVenue(Long venueId, String userPhone);

    /**
     * Lấy liên lạc theo owner (tất cả liên lạc liên quan đến các venue của owner)
     */
    List<LienLacDTO> getLienLacByOwner(String userPhone);

    /**
     * Lấy chi tiết một liên lạc
     */
    LienLacDTO getLienLacById(Long id, String userPhone);

    /**
     * Cập nhật trạng thái và phản hồi liên lạc (cho owner/admin)
     */
    LienLacDTO updateLienLac(Long id, LienLacUpdateRequest request, String userPhone);

    /**
     * Xóa liên lạc (chỉ admin hoặc owner của venue liên quan)
     */
    void deleteLienLac(Long id, String userPhone);
}
