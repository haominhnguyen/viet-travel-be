package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.model.TourBookingCustomer;
import com.fpt.capstone.tourism.repository.TourBookingCustomerRepository;
import com.fpt.capstone.tourism.service.TourBookingCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourBookingCustomerServiceImpl implements TourBookingCustomerService {

    private final TourBookingCustomerRepository tourBookingCustomerRepository;

    @Override
    public List<TourBookingCustomer> saveAll(List<TourBookingCustomer> customers) {
        return tourBookingCustomerRepository.saveAll(customers);
    }
}
