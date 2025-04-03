package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TagDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourImageDTO;
import com.fpt.capstone.tourism.mapper.*;
import com.fpt.capstone.tourism.model.Tag;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.repository.TagRepository;
import com.fpt.capstone.tourism.repository.TourImageRepository;
import com.fpt.capstone.tourism.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourServiceImplTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourMapper tourMapper;

    @Mock
    private LocationMapper locationMapper;

    @Mock
    private TourImageMapper tourImageMapper;

    @Mock
    private TourImageRepository tourImageRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TourServiceImpl tourService;

    private Tour mockTour;
    private PublicTourDTO mockTourDTO;

    @BeforeEach
    void setUp() {
        mockTour = new Tour();
        mockTour.setId(1L);
        mockTour.setName("Amazing Vietnam");
        mockTour.setNumberDays(5);
        mockTour.setNumberNights(4);

        // Create tags with full information
        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("Beach");
        tag1.setDescription("Beautiful beach destinations with white sand and blue sea.");
        tag1.setDeleted(false);
        tag1.setTours(new ArrayList<>());
        tag1.setBlogs(new ArrayList<>());

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("Adventure");
        tag2.setDescription("Exciting adventure experiences for thrill seekers.");
        tag2.setDeleted(false);
        tag2.setTours(new ArrayList<>());
        tag2.setBlogs(new ArrayList<>());

        mockTour.setTags(new ArrayList<>(Arrays.asList(tag1, tag2)));
        mockTour.setTourImages(new ArrayList<>());

        List<TagDTO> tagDTOs = mockTour.getTags().stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                .collect(Collectors.toList());

        mockTourDTO = PublicTourDTO.builder()
                .id(1L)
                .name("Amazing Vietnam")
                .numberDays(5)
                .numberNight(4)
                .tags(tagDTOs)
                .departLocation(null)
                .tourImages(Collections.emptyList())
                .priceFrom(100.0)
                .build();
    }



    @Test
    void testFindTopTourOfYear_ReturnsNewestTourWhenNoTopTourFound() {
        when(tourRepository.findTopTourIdsOfCurrentYear()).thenReturn(Collections.emptyList());
        when(tourRepository.findNewestTour()).thenReturn(mockTour);
        when(tourRepository.findMinSellingPriceForTours(1L)).thenReturn(100.0);

        PublicTourDTO result = tourService.findTopTourOfYear();

        assertNotNull(result);
        assertEquals(mockTour.getId(), result.getId());
        assertEquals("Amazing Vietnam", result.getName());
        verify(tourRepository).findNewestTour();
    }

    @Test
    void testFindTopTourOfYear_ReturnsTopTour() {
        List<Long> topTourIds = List.of(1L);
        when(tourRepository.findTopTourIdsOfCurrentYear()).thenReturn(topTourIds);
        when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
        when(tourRepository.findMinSellingPriceForTours(1L)).thenReturn(100.0);

        PublicTourDTO result = tourService.findTopTourOfYear();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Amazing Vietnam", result.getName());
        verify(tourRepository).findById(1L);
    }

    @Test
    void testFindTrendingTours_ReturnsTours() {
        List<Long> trendingTourIds = List.of(1L);
        List<Tour> trendingTours = List.of(mockTour);
        Pageable pageable = PageRequest.of(0, 5);

        when(tourRepository.findTrendingTourIds(pageable)).thenReturn(trendingTourIds);
        when(tourRepository.findAllById(trendingTourIds)).thenReturn(trendingTours);

        // Create a properly typed List of Object arrays
        List<Object[]> priceData = new ArrayList<>();
        priceData.add(new Object[]{1L, 100.0});

        when(tourRepository.findMinSellingPrices(trendingTourIds)).thenReturn(priceData);

        List<PublicTourDTO> result = tourService.findTrendingTours(5);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }



    @Test
    void testGetAllPublicTour_ReturnsPagedTours() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<Tour> tourPage = new PageImpl<>(List.of(mockTour), pageable, 1);
        lenient().when(tourRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(tourPage);
        List<Object[]> priceData = new ArrayList<>();
        priceData.add(new Object[]{1L, 100.0});
        when(tourRepository.findMinSellingPrices(anyList())).thenReturn(priceData);
        GeneralResponse<PagingDTO<List<PublicTourDTO>>> response = tourService.getAllPublicTour(0, 10, "", null, null, null, null, null, null);
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getTotal());
    }

    @Test
    void testFindSameLocationPublicTour_ReturnsTours() {
        List<Long> tourIds = List.of(1L);
        List<TagDTO> mockTags = Collections.emptyList();
        List<PublicTourImageDTO> mockImages = Collections.emptyList();

        when(tourRepository.findSameLocationTourIds(any())).thenReturn(tourIds);
        when(tourRepository.findById(1L)).thenReturn(Optional.of(mockTour));
        when(tagRepository.findTagsByTourId(1L)).thenReturn(Collections.emptyList());
        when(tourRepository.findMinSellingPriceForTours(1L)).thenReturn(100.0);
        when(tourImageRepository.findTourImagesByTourId(1L)).thenReturn(Collections.emptyList());

        List<PublicTourDTO> result = tourService.findSameLocationPublicTour(List.of(1L));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
