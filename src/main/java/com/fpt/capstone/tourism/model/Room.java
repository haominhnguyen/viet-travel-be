package com.fpt.capstone.tourism.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
@Data
@Entity
@Builder
@Table(name = "room")
@NoArgsConstructor
@AllArgsConstructor
public class Room extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "capacity", nullable = false)
    private Integer capacity; // Số khách tối đa trong phòng

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity; // Số lượng phòng còn lại


    @Column(name = "is_deleted")
    private Boolean deleted;

    @OneToOne
    @JoinColumn(name = "service_id", nullable = false, unique = true)
    private Service service; // Liên kết với bảng Service

    @Column(columnDefinition = "text")
    private String facilities; // Danh sách các dịch vụ trong phòng (bồn tắm, tủ lạnh, wifi)

//    @Column(name = "check_in_date")
//    private LocalDate checkInDate; // Thời gian check-in
//
//    @Column(name = "check_out_date")
//    private LocalDate checkOutDate; // Thời gian check-out
}
