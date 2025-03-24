package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByServiceId(Long serviceId);
    Optional<Room> findByServiceIdAndDeletedFalse(Long serviceId);
}
