package com.fpt.capstone.tourism.helper.IHelper;

public interface BookingHelper {

    String generateBookingCode(Long tourId, Long scheduleId, Long customerId);
}
