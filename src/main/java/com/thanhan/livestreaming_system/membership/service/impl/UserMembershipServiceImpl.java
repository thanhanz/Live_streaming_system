package com.thanhan.livestreaming_system.membership.service.impl;

import com.thanhan.livestreaming_system.membership.dto.request.StatisticsMembershipGet;
import com.thanhan.livestreaming_system.membership.dto.response.MembPackageResponse;
import com.thanhan.livestreaming_system.membership.dto.response.MembersStatisticSummaryResponse;
import com.thanhan.livestreaming_system.membership.dto.response.MembershipStatisticChart;
import com.thanhan.livestreaming_system.membership.dto.response.MonthTotalResponse;
import com.thanhan.livestreaming_system.membership.entity.MembershipPackage;
import com.thanhan.livestreaming_system.membership.entity.UserMembership;
import com.thanhan.livestreaming_system.membership.repository.UserMembershipRepository;
import com.thanhan.livestreaming_system.membership.service.MembershipPackageService;
import com.thanhan.livestreaming_system.membership.service.UserMembershipService;
import com.thanhan.livestreaming_system.user.entity.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserMembershipServiceImpl implements UserMembershipService {

    private static final Logger log = LoggerFactory.getLogger(UserMembershipServiceImpl.class);
    UserMembershipRepository userMembershipRepository;
    MembershipPackageService membershipPackageService;

    @Override
    public UserMembership createMembership(User u, MembershipPackage pkg) {
        UserMembership membership = new UserMembership();
        membership.setUser(u);
        membership.setMembershipPackage(pkg);
        membership.setCreatedAt(LocalDateTime.now());

        int duration = pkg.getDuration();
        membership.setEndAt(membership.getCreatedAt().plusDays(duration));
        return userMembershipRepository.save(membership);
    }

    @Override
    public Boolean checkMembership(Long channelId, UUID userId) {
        List<MembPackageResponse> pkgs = membershipPackageService.getPackageByChannelId(channelId);

        if (pkgs == null || pkgs.size() == 0) {
            return false;
        }

        for (MembPackageResponse pkg : pkgs) {
            if (userMembershipRepository.checkMembership(userId, pkg.id()))
                return true;
        }
        return false;
    }

    @Override
    public MembersStatisticSummaryResponse statisticMembershipByChannel(Long channelId, Integer year) {
        List<MembPackageResponse> pkgs = membershipPackageService.getPackageByChannelId(channelId);

        if (pkgs == null || pkgs.size() == 0) {
            return null;
        }

        List<Long> pkgIds = new ArrayList<>();
        pkgs.forEach(pkg -> {
            pkgIds.add(pkg.id());
        });

        List<StatisticsMembershipGet> statsFromDB = userMembershipRepository
                .statisticMembership(pkgIds, year == null ? Calendar.getInstance().get(Calendar.YEAR) : year)
                .stream().map(mapper -> new StatisticsMembershipGet(
                        mapper.getPackageId(),
                        mapper.getMonth(), mapper.getTotal())).toList();


        log.info("Get statistics from DB: {}", statsFromDB.size());

        //Nhóm
        Map<Long, List<StatisticsMembershipGet>> groupByPackage =
                statsFromDB.stream().collect(Collectors.groupingBy(StatisticsMembershipGet::packageId));

        List<MembershipStatisticChart> resultChart = new ArrayList<>();
        Long totalRevenue = 0L;
        Long countMembers = Long.valueOf(statsFromDB.size());

        for (Long pkgId : pkgIds) {
            // Tạo map {month -> total}
            Map<Integer, Long> monthToTotal = groupByPackage
                    .getOrDefault(pkgId, List.of())
                    .stream()
                    .collect(Collectors.toMap(
                            StatisticsMembershipGet::month,
                            StatisticsMembershipGet::total
                    ));

            List<MonthTotalResponse> data = new ArrayList<>();
            MembPackageResponse pkg = pkgs.stream().filter(p -> p.id() == pkgId).findFirst().get();
            for (int month = 1; month <= 12; month++) {
                totalRevenue += monthToTotal.getOrDefault(month, 0L) * pkg.price();
                data.add(new MonthTotalResponse(month, monthToTotal.getOrDefault(month, 0L)));
            }
            resultChart.add(new MembershipStatisticChart(pkg.name(), data));
        }
        return new MembersStatisticSummaryResponse(totalRevenue, countMembers, resultChart);
    }
}
