package com.fpt.capstone.tourism.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourDTO;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.mapper.TagMapper;
import com.fpt.capstone.tourism.mapper.TourImageMapper;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.repository.TourRepository;
import com.fpt.capstone.tourism.repository.TourScheduleRepository;
import com.fpt.capstone.tourism.service.TourService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import com.fpt.capstone.tourism.exception.common.BusinessException;

import java.time.LocalDate;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class TourServiceImplTest {


    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourScheduleRepository tourScheduleRepository;

    @Mock
    private LocationMapper locationMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private TourImageMapper tourImageMapper;

    @InjectMocks
    private TourServiceImpl tourService;

    private Page<Tour> tourPage;
    private List<Tour> tourList;




    @BeforeEach
    void setUp() {
        Tour tour1 = Tour.builder()
                .id(1L)
                .name("Hà Giang Tổ Quốc Adventure")
                .departLocation( new Location())
                .tags(new ArrayList<>())
                .tourImages(new ArrayList<>())
                .locations(new ArrayList<>())
                .tourSchedules(new ArrayList<>())
                .deleted(false)
                .build();
        Tour tour2 = Tour.builder()
                .id(2L)
                .name("Sa Pa Trekking")
                .numberDays(4)
                .numberNights(3)
                .departLocation( new Location())
                .tags(new ArrayList<>())
                .tourImages(new ArrayList<>())
                .locations(new ArrayList<>())
                .tourSchedules(new ArrayList<>())
                .deleted(false)
                .build();

        Tour tour3 = Tour.builder()
                .id(3L)
                .name("Nha Trang Trekking")
                .numberDays(4)
                .numberNights(3)
                .departLocation( new Location())
                .tags(new ArrayList<>())
                .tourImages(new ArrayList<>())
                .locations(new ArrayList<>())
                .tourSchedules(new ArrayList<>())
                .deleted(true)
                .build();

        tourList = List.of(tour1, tour2, tour3);
        tourPage = new PageImpl<>(tourList, PageRequest.of(0, 10, Sort.by("id").descending()), tourList.size());


    }

    @Test
    void testGetAllPublicTour_Success() {
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);
        when(tourRepository.findMinSellingPrices(anyList())).thenReturn(List.of(new Object[]{1L, 2000000.0}, new Object[]{2L, 3000000.0}));

        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(0, 10, "Hà Giang", 2000000.0, 5000000.0, 3, LocalDate.now(), 1L);

        assertNotNull(response);
        assertEquals("Hà Giang Tổ Quốc Adventure", response.getData().getItems().get(0).getName());
    }

    @Test
    void testGetAllPublicTour_EmptyResult() {
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(0, 10, "lan anh", 2000000.0, 5000000.0, 3, LocalDate.now(), 1L);

        assertNotNull(response);
        assertTrue(response.getData().getItems().isEmpty());
    }

    @Test
    void testGetAllPublicTour_ExceptionThrown() {
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenThrow(new RuntimeException("Database error"));

        BusinessException thrown = assertThrows(BusinessException.class,
                () -> tourService.getAllPublicTour(0, 10, null, 0.0, 10000000.0, null, null, null));

        assertEquals("Get all public tour fail", thrown.getMessage());
    }
    @Test
    void testGetAllPublicTour_BudgetFromZero() {
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);
        when(tourRepository.findMinSellingPrices(anyList())).thenReturn(List.of(new Object[]{1L, 1000000.0}, new Object[]{2L, 2000000.0}));

        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(0, 10, "Hà Giang", 0.0, 5000000.0, 3, LocalDate.now(), 1L);

        assertNotNull(response);
        assertFalse(response.getData().getItems().isEmpty());
    }

    @Test
    void testGetAllPublicTour_BudgetToMaxValue() {
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);
        when(tourRepository.findMinSellingPrices(anyList())).thenReturn(List.of(new Object[]{1L, 500000.0}, new Object[]{2L, 1000000.0}));

        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(0, 10, "Sa Pa", 1000000.0, Double.MAX_VALUE, 3, LocalDate.now(), 1L);

        assertNotNull(response);
        assertFalse(response.getData().getItems().isEmpty());
    }

    @Test
    void testGetAllPublicTour_DurationZero() {
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(0, 10, "Hà Giang", 1000000.0, 5000000.0, 0, LocalDate.now(), 1L);

        assertNotNull(response);
        assertTrue(response.getData().getItems().isEmpty());
    }

    @Test
    void testGetAllPublicTour_PaginationSorting() {
        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by("id").descending());
        Page<Tour> sortedPage = new PageImpl<>(tourList, pageRequest, tourList.size());

        when(tourRepository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(sortedPage);
        when(tourRepository.findMinSellingPrices(anyList())).thenReturn(List.of(new Object[]{1L, 1500000.0}, new Object[]{2L, 2500000.0}));

        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(0, 2, "Hà Giang", 1000000.0, 5000000.0, 3, LocalDate.now(), 1L);

        assertNotNull(response);
        assertEquals("Hà Giang Tổ Quốc Adventure", response.getData().getItems().get(0).getName());  // ID 2 trước ID 1
    }


    @Test
    void testFindToursWithSpecification() {
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);
        when(tourRepository.findMinSellingPrices(anyList())).thenReturn(List.of(new Object[]{1L, 1000000.0}, new Object[]{2L, 2000000.0}));

        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(0, 10, null, null, null, null, null, null);

        assertNotNull(response);
        assertFalse(response.getData().getItems().isEmpty());
    }

    @Test
    void testKeywordSearch() {
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);
        when(tourRepository.findMinSellingPrices(anyList())).thenReturn(List.of(new Object[]{1L, 1000000.0}, new Object[]{2L, 2000000.0}));

        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(0, 10, "Hà Giang", null, null, null, null, null);

        assertNotNull(response);
        assertFalse(response.getData().getItems().isEmpty());
    }

    @Test
    void testFilterByDuration() {
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);
        when(tourRepository.findMinSellingPrices(anyList())).thenReturn(List.of(new Object[]{1L, 1000000.0}, new Object[]{2L, 2000000.0}));

        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(0, 10, null, null, null, 3, null, null);

        assertNotNull(response);
        assertFalse(response.getData().getItems().isEmpty());
    }

    @Test
    void testFilterByPriceRange() {
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);
        when(tourRepository.findMinSellingPrices(anyList())).thenReturn(List.of(new Object[]{1L, 1000000.0}, new Object[]{2L, 2000000.0}));

        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(0, 10, null, 100.0, 200.0, null, null, null);

        assertNotNull(response);
        assertFalse(response.getData().getItems().isEmpty());
    }

    @Test
    void testFilterByFromDate() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);
        when(tourRepository.findMinSellingPrices(anyList())).thenReturn(List.of(new Object[]{1L, 1000000.0}, new Object[]{2L, 2000000.0}));

        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(0, 10, null, null, null, null, date, null);

        assertNotNull(response);
        assertFalse(response.getData().getItems().isEmpty());
    }

    @Test
    void testFilterByDepartLocation() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(tourPage);
        when(tourRepository.findMinSellingPrices(anyList())).thenReturn(List.of(new Object[]{1L, 1000000.0}, new Object[]{2L, 2000000.0}));

        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(0, 10, null, null, null, null, null, 1L);

        assertNotNull(response);
        assertFalse(response.getData().getItems().isEmpty());
    }



}
