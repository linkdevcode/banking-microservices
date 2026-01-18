package com.linkdevcode.banking.voucher_service.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.linkdevcode.banking.voucher_service.client.HistoryClient;
import com.linkdevcode.banking.voucher_service.client.request.GetTopUserRequest;
import com.linkdevcode.banking.voucher_service.client.response.TopUserStatistic;
import com.linkdevcode.banking.voucher_service.entity.Voucher;
import com.linkdevcode.banking.voucher_service.enumeration.EVoucherStatus;
import com.linkdevcode.banking.voucher_service.enumeration.EVoucherType;
import com.linkdevcode.banking.voucher_service.model.request.AdminVoucherSearchRequest;
import com.linkdevcode.banking.voucher_service.model.request.UserVoucherSearchRequest;
import com.linkdevcode.banking.voucher_service.model.response.AdminVoucherSearchResponse;
import com.linkdevcode.banking.voucher_service.model.response.UserVoucherSearchResponse;
import com.linkdevcode.banking.voucher_service.repository.VoucherRepository;
import com.linkdevcode.banking.voucher_service.repository.specification.VoucherSpecifications;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoucherService {
    
    private final HistoryClient historyClient;
    private final VoucherRepository voucherRepository;

    @Transactional
    public void generateDailyVouchers(LocalDate targetDate) {

        List<TopUserStatistic> topUsers =
            historyClient.getTopUsers(new GetTopUserRequest(
                targetDate,
                targetDate,
                10
            ));

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);

        int dailyVoucherCount = 0;
        for (TopUserStatistic user : topUsers) {

            dailyVoucherCount = voucherRepository.countByUserIdAndTypeAndIssuedAtBetween(
                user.getUserId(), EVoucherType.TOP_TRANSFER_DAILY, startOfDay, endOfDay
            );

            if (dailyVoucherCount > 0) {
                continue;
            }

            Voucher voucher = new Voucher();
            voucher.setUserId(user.getUserId());
            voucher.setCode(generateCode());
            voucher.setType(EVoucherType.TOP_TRANSFER_DAILY);
            voucher.setValue(BigDecimal.valueOf(5000));
            voucher.setStatus(EVoucherStatus.ACTIVE);
            voucher.setIssuedAt(LocalDateTime.now());
            voucher.setExpiredAt(LocalDateTime.now().plusDays(30));

            voucherRepository.save(voucher);
        }
    }

    // Search transactions for a specific user
    public Page<UserVoucherSearchResponse> searchForUser(
        Long userId,
        UserVoucherSearchRequest request
    ){
        Pageable pageable = buildPageable(request);

        Specification<Voucher> spec = Specification
            .where(VoucherSpecifications.hasUserId(userId))
            .and(VoucherSpecifications.hasVoucherType(request.getVoucherType()))
            .and(VoucherSpecifications.hasVoucherStatus(request.getVoucherStatus()))
            .and(VoucherSpecifications.isValidAt(LocalDateTime.now()));
        
        return voucherRepository.findAll(spec, pageable)
                .map(this::mapToUserResponse);
    }

    // Search transactions for admin
    public Page<AdminVoucherSearchResponse> searchForAdmin(
        AdminVoucherSearchRequest request
    ){
        Pageable pageable = buildPageable(request);

        Specification<Voucher> spec = Specification
            .where(VoucherSpecifications.hasUserId(request.getUserId()))
            .and(VoucherSpecifications.hasVoucherType(request.getVoucherType()))
            .and(VoucherSpecifications.hasVoucherStatus(request.getVoucherStatus()))
            .and(VoucherSpecifications.isValidAt(LocalDateTime.now()));
        
        return voucherRepository.findAll(spec, pageable)
                .map(this::mapToAdminResponse);
    }

    // Build pageable from request
    private Pageable buildPageable(Object request){
        String sortBy = request instanceof UserVoucherSearchRequest r
            ? r.getSortBy()
            : ((AdminVoucherSearchRequest) request).getSortBy();
        
        String direction = request instanceof UserVoucherSearchRequest r
            ? r.getDirection()
            : ((AdminVoucherSearchRequest) request).getDirection();

        int page = request instanceof UserVoucherSearchRequest r
            ? r.getPage()
            : ((AdminVoucherSearchRequest) request).getPage();
        
        int size = request instanceof UserVoucherSearchRequest r
            ? r.getSize()
            : ((AdminVoucherSearchRequest) request).getSize();
        
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        return PageRequest.of(page, size, sort);
    }

    private String generateCode() {
        return "VC-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    // Map entity to user response
    private UserVoucherSearchResponse mapToUserResponse(Voucher voucher) {
        return new UserVoucherSearchResponse(
            voucher.getCode(),
            voucher.getValue(),
            voucher.getType(),
            voucher.getStatus(),
            voucher.getIssuedAt(),
            voucher.getExpiredAt()
        );
    }

    // Map entity to admin response
    private AdminVoucherSearchResponse mapToAdminResponse(Voucher voucher) {
        return new AdminVoucherSearchResponse(
            voucher.getUserId(),
            voucher.getId(),
            voucher.getCode(),
            voucher.getValue(),
            voucher.getType(),
            voucher.getStatus(),
            voucher.getIssuedAt(),
            voucher.getExpiredAt()
        );
    }
}
