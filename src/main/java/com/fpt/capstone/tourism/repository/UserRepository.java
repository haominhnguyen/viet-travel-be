package com.fpt.capstone.tourism.repository;


import com.fpt.capstone.tourism.dto.common.NewUsersChartDTO;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserById(Long id);
    User findUserByEmailContainsIgnoreCase(String email);
    User findUserByEmailAndPassword(String username,String password);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByPhone(String phone);
    Page<User> findAll(Specification<User> spec, Pageable pageable);


    @Query("SELECT u FROM User u JOIN u.userRoles ur " +
            "WHERE ur.role.roleName = :roleName " +
            "AND (u.deleted IS NULL OR u.deleted = false) " +
            "AND (ur.deleted IS NULL OR ur.deleted = false) " +
            "AND (ur.role.deleted IS NULL OR ur.role.deleted = false) " +
            "AND (:fullName IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :fullName, '%')))")
    List<User> findUsersByRoleNameAndFullNameLike(
            @Param("roleName") String userRole,
            @Param("fullName") String fullName
    );

    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.role.id = 10")
    Page<User> findAllTourGuides(Specification<User> spec,Pageable pageable);

    @Query("""
    SELECT u FROM User u
        WHERE u.id NOT IN (
            SELECT ts.tourGuide.id FROM TourSchedule ts
            WHERE ts.tourGuide IS NOT NULL
            AND ts.startDate < (SELECT s.endDate FROM TourSchedule s WHERE s.id = :scheduleId)
            AND ts.endDate > (SELECT s.startDate FROM TourSchedule s WHERE s.id = :scheduleId)
        )
        AND u.id IN (
            SELECT ur.user.id FROM UserRole ur WHERE ur.role.roleName = 'TOUR_GUIDE'
        )
        AND u.deleted = FALSE
""")
    List<User> findAvailableTourGuideByScheduleId(@Param("scheduleId") Long scheduleId);

    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.userRoles ur " +
            "WHERE ur.role.id = :roleId " +
            "AND ur.deleted = false")
    List<User> findUsersByRoleAndActive(
            @Param("roleId") Long roleId,
            @Param("active") boolean active);

    @Query("""
            SELECT new com.fpt.capstone.tourism.dto.common.NewUsersChartDTO(
            EXTRACT(MONTH FROM u.createdAt),
            EXTRACT(YEAR FROM u.createdAt),
            COUNT(u.id)
            ) 
            FROM User u
            WHERE DATE(u.createdAt) BETWEEN :startDate AND :endDate 
            AND u.emailConfirmed = TRUE 
            GROUP BY EXTRACT(YEAR FROM u.createdAt), EXTRACT(MONTH FROM u.createdAt) 
            ORDER BY EXTRACT(YEAR FROM u.createdAt) DESC, EXTRACT(MONTH FROM u.createdAt) DESC
            """)
    List<NewUsersChartDTO> getNewUserByMonth(LocalDate startDate, LocalDate endDate);

    @Query(value = """
SELECT u.id FROM User u
WHERE u.username = :name
""")
    Long findIdByUsername(String name);
}
