package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.model.TourBookingCustomer;

import java.util.List;

public interface TourBookingCustomerService {
    List<TourBookingCustomer> saveAll(List<TourBookingCustomer> customers);
}
