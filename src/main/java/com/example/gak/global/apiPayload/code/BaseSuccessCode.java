package com.example.gak.global.apiPayload.code;

import com.example.gak.global.apiPayload.dto.ReasonDTO;

public interface BaseSuccessCode {

    ReasonDTO getReason();

    ReasonDTO getReasonHttpStatus();
}
