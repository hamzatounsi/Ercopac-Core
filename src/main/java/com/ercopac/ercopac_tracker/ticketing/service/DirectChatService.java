package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.ticketing.domain.DirectChatMessage;
import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import com.ercopac.ercopac_tracker.ticketing.repository.DirectChatMessageRepository;
import com.ercopac.ercopac_tracker.user.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;

/**
 * Live chat: one-to-one conversations between the current user and another user of the same organisation.
 * Support staff can chat with other staff and with clients; a client can only chat with support staff.
 */
@Service @Transactional
public class DirectChatService {
 private static final List<Role> STAFF_ROLES=List.of(Role.SALES_MANAGER_LEAD,Role.H24,Role.H24_LEAD);
 private static final List<Role> STAFF_AND_CLIENT_ROLES=List.of(Role.SALES_MANAGER_LEAD,Role.H24,Role.H24_LEAD,Role.CLIENT);
 private final DirectChatMessageRepository messages; private final UserRepository users; private final SecurityUtils security;
 public DirectChatService(DirectChatMessageRepository messages,UserRepository users,SecurityUtils security){this.messages=messages;this.users=users;this.security=security;}

 public List<ChatContact> contacts(){AppUser me=current();return users.findByOrganisation_IdAndRoleInAndActiveTrueOrderByFullNameAsc(orgId(me),isStaff(me)?STAFF_AND_CLIENT_ROLES:STAFF_ROLES).stream().filter(u->!u.getId().equals(me.getId())).map(this::contact).toList();}

 public List<ChatConversation> conversations(){AppUser me=current();Map<Long,ChatConversation> byPartner=new LinkedHashMap<>();Map<Long,Long> unread=new HashMap<>();
  for(DirectChatMessage m:messages.involving(orgId(me),me.getId())){boolean mine=m.getSender().getId().equals(me.getId());AppUser partner=mine?m.getRecipient():m.getSender();
   if(!mine&&m.getReadAt()==null)unread.merge(partner.getId(),1L,Long::sum);
   byPartner.putIfAbsent(partner.getId(),new ChatConversation(contact(partner),m.getMessage(),m.getCreatedAt(),mine,0));}
  return byPartner.values().stream().map(c->new ChatConversation(c.contact(),c.lastMessage(),c.lastMessageAt(),c.lastFromMe(),unread.getOrDefault(c.contact().id(),0L))).toList();}

 public List<ChatMessageDto> messages(Long partnerId){AppUser me=current();AppUser partner=partner(me,partnerId);messages.markRead(orgId(me),me.getId(),partner.getId(),Instant.now());return messages.thread(orgId(me),me.getId(),partner.getId()).stream().map(this::message).toList();}

 public ChatMessageDto send(Long partnerId,ChatMessageRequest r){AppUser me=current();AppUser partner=partner(me,partnerId);String text=r.message()==null?"":r.message().trim();if(text.isEmpty())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Message cannot be blank.");
  DirectChatMessage m=new DirectChatMessage();m.setOrganisation(me.getOrganisation());m.setSender(me);m.setRecipient(partner);m.setMessage(text);return message(messages.save(m));}

 private AppUser partner(AppUser me,Long partnerId){AppUser p=users.findByIdAndOrganisation_Id(partnerId,orgId(me)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found in your organisation."));
  boolean allowed=!p.getId().equals(me.getId())&&p.isActive()&&(isStaff(p)||(isStaff(me)&&p.hasRole(Role.CLIENT)));
  if(!allowed)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You cannot chat with this user.");return p;}
 private AppUser current(){AppUser me=users.findById(security.getCurrentUserId()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Current account no longer exists."));if(!isStaff(me)&&!me.hasRole(Role.CLIENT))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Live chat is not available for your role.");return me;}
 private Long orgId(AppUser u){if(u.getOrganisation()==null)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Live chat requires an organisation.");return u.getOrganisation().getId();}
 private boolean isStaff(AppUser u){return u.hasAnyRole(STAFF_ROLES);}
 private ChatContact contact(AppUser u){return new ChatContact(u.getId(),u.getFullName(),u.getEmail(),isStaff(u)?"SUPPORT":"CLIENT",isStaff(u));}
 private ChatMessageDto message(DirectChatMessage m){return new ChatMessageDto(m.getId(),m.getSender().getId(),m.getRecipient().getId(),m.getMessage(),m.getCreatedAt(),m.getReadAt());}
}
