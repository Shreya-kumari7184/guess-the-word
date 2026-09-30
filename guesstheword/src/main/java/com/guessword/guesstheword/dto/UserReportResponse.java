package com.guessword.guesstheword.dto;

import java.util.List;

public record UserReportResponse(Long userId, String username, List<UserReportEntry> reports) {
}
