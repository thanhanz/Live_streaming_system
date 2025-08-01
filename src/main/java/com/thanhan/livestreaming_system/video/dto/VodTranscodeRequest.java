package com.thanhan.livestreaming_system.video.dto;

import java.io.Serializable;

public record VodTranscodeRequest(Long vodId, String vodStorageKey) implements Serializable {
}
