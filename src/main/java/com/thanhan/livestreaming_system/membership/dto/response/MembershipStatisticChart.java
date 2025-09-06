package com.thanhan.livestreaming_system.membership.dto.response;

import java.util.List;

public record MembershipStatisticChart(String packageName, List<MonthTotalResponse> monthTotalResponses) {

}
