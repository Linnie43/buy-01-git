package com.buy01.user.client;

import com.buy01.user.dto.AvatarCreateDTO;
import com.buy01.user.dto.AvatarResponseDTO;
import com.buy01.user.dto.AvatarUpdateRequest;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Service
public class MediaClient {

    private final RestTemplate restTemplate;
    private static final String MEDIA_SERVICE_BASE_URL = "http://media-service:8082/api/media";
    private static final Logger log = LoggerFactory.getLogger(MediaClient.class);

    public MediaClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    // send avatar to media service and get back the URL path
    public AvatarResponseDTO saveAvatar(AvatarCreateDTO avatarCreateDTO) throws IOException {
        String url = MEDIA_SERVICE_BASE_URL + "/internal/avatar";

        MultipartFile avatar = avatarCreateDTO.getAvatar();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("avatar", new ByteArrayResource(avatar.getBytes()) {
            @Override
            public String getFilename() {
                return avatar.getOriginalFilename();
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<AvatarResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                AvatarResponseDTO.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Avatar download failed, response code: {}", response.getStatusCode());
            throw new FileUploadException("Failed to upload avatar: " + response.getStatusCode());
        }

        return response.getBody();
    }

    public AvatarResponseDTO updateAvatar(AvatarUpdateRequest avatarUpdateRequest) throws IOException, FileUploadException {
        String url = MEDIA_SERVICE_BASE_URL + "/internal/avatar";

        MultipartFile avatar = avatarUpdateRequest.getNewAvatar();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("oldAvatar", avatarUpdateRequest.getOldAvatar());
        body.add("newAvatar", new ByteArrayResource(avatar.getBytes()) {
            @Override
            public String getFilename() {
                return avatar.getOriginalFilename();
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);


        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<AvatarResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    AvatarResponseDTO.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }

    }
}
