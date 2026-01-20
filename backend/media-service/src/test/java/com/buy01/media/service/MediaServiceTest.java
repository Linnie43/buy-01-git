package com.buy01.media.service;

import com.buy01.media.dto.MediaResponseDTO;
import com.buy01.media.exception.FileUploadException;
import com.buy01.media.model.Media;
import com.buy01.media.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @TempDir
    Path tempDir;

    static class TestMedia extends Media {

        TestMedia(String id, String name, String path, String productId) {
            super(id, name, path, productId);
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        mediaService = new MediaService(mediaRepository);
    }

    // -- PRODUCT IMAGE TESTS --

    // Testing saving valid product images - expected to return list of MediaResponseDTO
    @Test
    @DisplayName("saveProductImages with valid files returns MediaResponseDTO list")
    void saveProductImagesValid() throws IOException {
        MultipartFile file1 = new MockMultipartFile(
                "file1", "image1.png", "image/png", "dummy content 1".getBytes()
        );
        MultipartFile file2 = new MockMultipartFile(
                "file2", "image2.jpg", "image/jpeg", "dummy content 2".getBytes()
        );

        // Mock save() to return Media with IDs
        when(mediaRepository.save(any(Media.class)))
                .thenAnswer(invocation -> {
                    Media m = invocation.getArgument(0);
                    return new TestMedia("id-" + m.getName(), m.getName(), m.getPath(), m.getProductId());
                });

        List<MediaResponseDTO> result = mediaService.saveProductImages("product123", java.util.List.of(file1, file2));

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // Testing saving invalid product images (one file empty) - expected to throw FileUploadException
    @Test
    @DisplayName("saveProductImages with one empty file throws FileUploadException")
    void saveProductImagesOnEmptyFileFail() {
        MultipartFile file1 = new MockMultipartFile("file1", "image1.png", "image/png", "dummy content 1".getBytes());
        MultipartFile file2 = new MockMultipartFile("file2", "empty.jpg", "image/jpeg", new byte[0]);

        assertThrows(FileUploadException.class, () ->
            mediaService.saveProductImages("product123", java.util.List.of(file1, file2))
        );
    }

    // Testing updating product images with one deleted and one new file - expected to return list of MediaResponseDTO
    @Test
    @DisplayName("updateProductImages should delete specified images and add new ones")
    void updateProductImages() throws IOException {
        // given
        String productId = "product-1";
        String deleteId = "media-1";

        // Create temporary files for delete simulation
        Path file1Path = tempDir.resolve("img1.jpg");
        Path file2Path = tempDir.resolve("img2.jpg");
        Files.createFile(file1Path);
        Files.createFile(file2Path);

        Media existing1 = new TestMedia("media-1", "img1.jpg", file1Path.toString(), productId);
        Media existing2 = new TestMedia("media-2", "img2.jpg", file2Path.toString(), productId);

        // Mock repository behavior
        when(mediaRepository.findById("media-1")).thenReturn(Optional.of(existing1));
        lenient().when(mediaRepository.findById("media-2")).thenReturn(Optional.of(existing2));
        when(mediaRepository.getMediaByProductId(productId)).thenReturn(List.of(existing1, existing2));
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> {
            Media saved = invocation.getArgument(0);
            return new TestMedia("media-3", saved.getName(), saved.getPath(), saved.getProductId());
        });

        MockMultipartFile newImage = new MockMultipartFile("file", "new.jpg", "image/jpeg", "test".getBytes());

        // Perform update
        List<MediaResponseDTO> result = mediaService.updateProductImages(
                productId,
                List.of(deleteId),
                List.of(newImage)
        );


        // Assertions
        assertEquals(2, result.size());
        assertFalse(Files.exists(file1Path)); // deleted file
        assertTrue(Files.exists(file2Path));  // untouched file

        verify(mediaRepository).deleteById("media-1");
        verify(mediaRepository).save(any(Media.class));
    }


    // -- AVATAR TESTS --

    // Testing saving valid user avatar - expected to return file name
    @Test
    @DisplayName("saveUserAvatar with valid file returns file name")
    void saveUserAvatarSuccess() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", "dummy content".getBytes()
        );

        String result = mediaService.saveUserAvatar(file);

        assertNotNull(result);
        assertTrue(result.endsWith(".png"));
    }

    // Testing saving invalid user avatar (empty file) - expected to throw FileUploadException
    @Test
    @DisplayName("saveUserAvatar with empty file throws FileUploadException")
    void saveUserAvatarFail() {
        MultipartFile file = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]
        );

        assertThrows(FileUploadException.class, () -> mediaService.saveUserAvatar(file));
    }

    // Testing saving invalid user avatar (wrong content type) - expected to throw FileUploadException
    @Test
    @DisplayName("saveUserAvatar with invalid content type throws FileUploadException")
    void saveUserAvatarContentFail() {
        MultipartFile file = new MockMultipartFile(
                "file", "picture.avif", "application/octet-stream", "dummy content".getBytes()
        );

        assertThrows(FileUploadException.class, () -> mediaService.saveUserAvatar(file));
    }

    // -- KAFKA CONSUMER TESTS --

    @Test
    @DisplayName("deleteMediaByProductId deletes files and repository entries")
    void deleteMediaByProductId() throws IOException {
        String productId = "product-123";

        // Temporary files for testing delete
        Path file1 = tempDir.resolve("img1.jpg");
        Path file2 = tempDir.resolve("img2.jpg");
        Files.createFile(file1);
        Files.createFile(file2);

        Media media1 = new TestMedia("media-1", "img1.jpg", file1.toString(), productId);
        Media media2 = new TestMedia("media-2", "img2.jpg", file2.toString(), productId);

        // Mock repository to return these media
        when(mediaRepository.getMediaByProductId(productId))
                .thenReturn(List.of(media1, media2));

        // Spy on service to allow real deleteFile execution
        MediaService spyService = spy(new MediaService(mediaRepository));

        // Call the method (simulating Kafka consumer trigger)
        spyService.deleteMediaByProductId(productId);

        // Verify repository calls
        verify(mediaRepository).getMediaByProductId(productId);
        verify(mediaRepository).delete(media1);
        verify(mediaRepository).delete(media2);

        // Verify files are deleted
        assertFalse(Files.exists(file1));
        assertFalse(Files.exists(file2));
    }

}
