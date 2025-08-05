package com.thanhan.livestreaming_system.membership.repository;

import com.thanhan.livestreaming_system.membership.entity.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
}
