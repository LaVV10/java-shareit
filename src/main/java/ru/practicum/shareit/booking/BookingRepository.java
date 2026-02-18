package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime now1, LocalDateTime now2, Pageable pageable);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(
            Long bookerId, LocalDateTime now, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(
            Long bookerId, LocalDateTime now, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(
            Long bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findByItemIdInAndStartBeforeAndEndAfterOrderByStartDesc(
            List<Long> itemIds, LocalDateTime now1, LocalDateTime now2);

    List<Booking> findByItemIdInAndEndBeforeOrderByStartDesc(
            List<Long> itemIds, LocalDateTime now);

    List<Booking> findByItemIdInAndStartAfterOrderByStartDesc(
            List<Long> itemIds, LocalDateTime now);

    List<Booking> findByItemIdInAndStatusOrderByStartDesc(
            List<Long> itemIds, BookingStatus status);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);

    List<Booking> findByItemIdAndStatusNotOrderByStartAsc(Long itemId, BookingStatus status);

    List<Booking> findByItemIdInAndStatusNotOrderByStartAsc(List<Long> itemIds, BookingStatus status);

    boolean existsByBookerIdAndItemIdAndEndBefore(Long bookerId, Long itemId, LocalDateTime end);
}
