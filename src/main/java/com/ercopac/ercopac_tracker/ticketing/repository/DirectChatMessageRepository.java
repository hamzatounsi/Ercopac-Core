package com.ercopac.ercopac_tracker.ticketing.repository; import com.ercopac.ercopac_tracker.ticketing.domain.DirectChatMessage; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.time.Instant; import java.util.*;
public interface DirectChatMessageRepository extends JpaRepository<DirectChatMessage,Long> {
 @Query("select m from DirectChatMessage m join fetch m.sender join fetch m.recipient where m.organisation.id=:org and ((m.sender.id=:a and m.recipient.id=:b) or (m.sender.id=:b and m.recipient.id=:a)) order by m.createdAt asc, m.id asc")
 List<DirectChatMessage> thread(@Param("org") Long organisationId,@Param("a") Long userA,@Param("b") Long userB);
 @Query("select m from DirectChatMessage m join fetch m.sender join fetch m.recipient where m.organisation.id=:org and (m.sender.id=:u or m.recipient.id=:u) order by m.createdAt desc, m.id desc")
 List<DirectChatMessage> involving(@Param("org") Long organisationId,@Param("u") Long userId);
 @Modifying @Query("update DirectChatMessage m set m.readAt=:now where m.organisation.id=:org and m.recipient.id=:me and m.sender.id=:other and m.readAt is null")
 int markRead(@Param("org") Long organisationId,@Param("me") Long me,@Param("other") Long other,@Param("now") Instant now);
}
