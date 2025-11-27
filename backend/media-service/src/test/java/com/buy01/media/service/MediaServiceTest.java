package com.buy01.media.service;

import com.buy01.media.dto.MediaResponseDTO;
import com.buy01.media.exception.FileUploadException;
import com.buy01.media.model.Media;
import com.buy01.media.repository.MediaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @InjectMocks
    private MediaService mediaService;

    public MediaServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    // -- PRODUCT IMAGE TESTS --

    // Testing saving valid product images - expected to return list of MediaResponseDTO
    @Test
    void saveProductImages_validFiles_returnsMediaResponseDTOList() throws IOException {
        MultipartFile file1 = new MockMultipartFile(
                "file1", "image1.png", "image/png", "dummy content 1".getBytes()
        );
        MultipartFile file2 = new MockMultipartFile(
                "file2", "image2.jpg", "image/jpeg", "dummy content 2".getBytes()
        );

        var result = mediaService.saveProductImages("product123", java.util.List.of(file1, file2));

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // Testing saving invalid product images (one file empty) - expected to throw FileUploadException
    @Test
    void saveProductImages_oneEmptyFile_throwsException() {
        MultipartFile file1 = new MockMultipartFile(
                "file1", "image1.png", "image/png", "dummy content 1".getBytes()
        );
        MultipartFile file2 = new MockMultipartFile(
                "file2", "empty.jpg", "image/jpeg", new byte[0]
        );
        assertThrows(FileUploadException.class, () ->
            mediaService.saveProductImages("product123", java.util.List.of(file1, file2))
        );
    }

    // Testing updating product images with one deleted and one new file - expected to return list of MediaResponseDTO
    @Test
    void updateProductImages_withOnlyAdditions_returnsUpdatedList() throws Exception {
        String productId = "product123";
        List<String> deletedIds = List.of();

        // New files
        MultipartFile newFile = new MockMultipartFile(
                "newFile", "newImage.png", "image/png", "dummy content".getBytes()
        );
        MultipartFile newFile2 = new MockMultipartFile(
                "newFile2", "newImage2.png", "image/png", "dummy content 2".getBytes()
        );
        List<MultipartFile> newImages = List.of(newFile, newFile2);

        // Spy on the service to mock saveImage
        MediaService spyService = spy(mediaService);

        // Mock saveImage to return a Media for each file
        doAnswer(invocation -> {
                    MultipartFile file = invocation.getArgument(0);
                    return new Media(file.getOriginalFilename(), "path/to/" + file.getOriginalFilename(), productId);
        }).when(spyService).saveImage(any(MultipartFile.class), eq(productId));

        // No existing media in DB
        when(mediaRepository.getMediaByProductId(productId)).thenReturn(List.of());

        // Call the method
        List<MediaResponseDTO> result = spyService.updateProductImages(productId, deletedIds, newImages);

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size()); // two new files
    }


    // -- AVATAR TESTS --

    // Testing saving valid user avatar - expected to return file name
    @Test
    void saveUserAvatar_validFile_returnsFileName() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", "dummy content".getBytes()
        );

        String result = mediaService.saveUserAvatar(file);

        assertNotNull(result);
        assertTrue(result.endsWith(".png"));
    }

    // Testing saving invalid user avatar (empty file) - expected to throw FileUploadException
    @Test
    void saveUserAvatar_emptyFile_throwsException() {
        MultipartFile file = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]
        );

        assertThrows(FileUploadException.class, () -> mediaService.saveUserAvatar(file));
    }

    // Testing saving invalid user avatar (wrong content type) - expected to throw FileUploadException
    @Test
    void saveUserAvatar_invalidContentType_throwsException() {
        MultipartFile file = new MockMultipartFile(
                "file", "picture.avif", "application/octet-stream", "dummy content".getBytes()
        );

        assertThrows(FileUploadException.class, () -> mediaService.saveUserAvatar(file));
    }
}
