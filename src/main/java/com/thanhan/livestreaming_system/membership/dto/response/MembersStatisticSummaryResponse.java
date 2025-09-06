package com.thanhan.livestreaming_system.membership.dto.response;

import java.util.List;

public record MembersStatisticSummaryResponse(Long totalRevenue, Long totalMembers, List<MembershipStatisticChart> dataChart) {
}
