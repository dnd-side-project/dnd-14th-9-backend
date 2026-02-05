package com.example.gak.global.apiPayload.exception;

import com.example.gak.global.apiPayload.code.BaseErrorCode;

public class S3Exception extends GeneralException{

    public S3Exception(BaseErrorCode errorCode) {
        super(errorCode);
    }
}