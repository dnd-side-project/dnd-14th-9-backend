package com.example.gak.global.validator;

import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class ImageFileValidator {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final long MAX_SIZE = 5 * 1024 * 1024;

    public void validate(MultipartFile image) {
        if (image.getSize() > MAX_SIZE) {
            throw new GeneralException(GeneralErrorCode.FILE_TOO_LARGE);
        }

        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new GeneralException(GeneralErrorCode.INVALID_IMAGE_TYPE);
        }
    }
}
