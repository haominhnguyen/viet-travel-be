package com.fpt.capstone.tourism.helper;

import com.fpt.capstone.tourism.helper.IHelper.BookingHelper;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class BookingHelperImpl implements BookingHelper {

    @Override
    public String generateBookingCode(Long tourId, Long scheduleId, Long customerId) {
        // Get current date in DDMMYY format
        String datePart = new SimpleDateFormat("ddMMyy").format(new Date());

        // Extract last 4 digits of the customer ID (ensuring at least 4 digits)
        String customerPart = "C" + String.format("%04d", customerId % 10000);


        String millisPart = String.valueOf(System.currentTimeMillis() % 1000);

        // Construct the booking code
        return String.format("%sVT%dSD%d%s-%s", datePart, tourId, scheduleId, customerPart, millisPart);
    }
}
