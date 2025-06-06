package com.fpt.capstone.tourism.model;

import com.fpt.capstone.tourism.model.enums.BookingServiceStatus;
import com.fpt.capstone.tourism.model.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceBookingDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private ServiceBooking bookingService;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "check_in_date", nullable = false)
    private LocalDateTime checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDateTime checkOutDate;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "total_price", nullable = false)
    private double price;

    @Enumerated(EnumType.STRING)
    private BookingServiceStatus status; // PENDING, CONFIRMED, CANCELED

    @Column(name = "note")
    private String note;

    @Column(name = "is_deleted")
    private Boolean deleted = false;

}
