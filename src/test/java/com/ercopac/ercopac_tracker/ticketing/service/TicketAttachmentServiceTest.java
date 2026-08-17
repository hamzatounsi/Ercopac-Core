package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.ticketing.domain.Ticket;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketAttachment;
import com.ercopac.ercopac_tracker.ticketing.repository.TicketAttachmentRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketAttachmentServiceTest {

    @TempDir Path storageRoot;
    @Mock TicketAttachmentRepository repository;
    @Mock TicketService tickets;

    private TicketAttachmentService service;
    private Ticket ticket;
    private AppUser user;

    @BeforeEach
    void setUp() {
        service = new TicketAttachmentService(repository, tickets, storageRoot.toString());
        ticket = new Ticket();
        user = new AppUser();
        ReflectionTestUtils.setField(user, "id", 42L);
        user.setFullName("Uploader");
        user.setEmail("uploader@example.test");
        user.setRole(Role.CLIENT);
        when(tickets.requireAccessible(1L)).thenReturn(ticket);
        when(tickets.currentUser()).thenReturn(user);
        when(repository.save(any(TicketAttachment.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void storesAllowedTypesWithServerGeneratedNamesAndSupportsDownloadAndDelete() throws Exception {
        for (UploadCase upload : List.of(
                new UploadCase("report.pdf", "application/pdf", ".pdf"),
                new UploadCase("image.png", "image/png", ".png"),
                new UploadCase("image.jpeg", "image/jpeg", ".jpg"),
                new UploadCase("notes.txt", "text/plain", ".txt"),
                new UploadCase("document.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx"))) {
            service.upload(1L, file(upload.fileName(), upload.contentType()));
            TicketAttachment attachment = savedAttachment();
            Path stored = storageRoot.resolve(attachment.getStoragePath()).normalize();

            assertThat(attachment.getStoredFileName()).matches("[0-9a-f-]{36}" + upload.extension());
            assertThat(stored).startsWith(storageRoot);
            assertThat(Files.readString(stored)).isEqualTo("attachment data");

            when(repository.findByIdAndTicket_Id(9L, 1L)).thenReturn(Optional.of(attachment));
            Resource download = service.download(1L, 9L);
            assertThat(download.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).isEqualTo("attachment data");

            when(tickets.mayManageCurrent()).thenReturn(true);
            service.delete(1L, 9L);
            assertThat(Files.exists(stored)).isFalse();
            verify(repository).delete(attachment);
            clearInvocations(repository);
        }
    }

    @Test
    void keepsTraversalLikeNamesOutOfTheStoragePath() {
        for (String originalName : List.of("../../evil.pdf", "..\\..\\evil.pdf", "folder/evil.pdf")) {
            service.upload(1L, file(originalName, "application/pdf"));
            TicketAttachment attachment = savedAttachment();

            assertThat(attachment.getOriginalFileName()).doesNotContain("/").doesNotContain("\\");
            assertThat(attachment.getStoragePath()).matches("[0-9a-f-]{36}\\.pdf");
            assertThat(storageRoot.resolve(attachment.getStoragePath()).normalize()).startsWith(storageRoot);
        }
    }

    private MockMultipartFile file(String name, String contentType) {
        return new MockMultipartFile("file", name, contentType, "attachment data".getBytes());
    }

    private TicketAttachment savedAttachment() {
        ArgumentCaptor<TicketAttachment> capture = ArgumentCaptor.forClass(TicketAttachment.class);
        verify(repository, atLeastOnce()).save(capture.capture());
        return capture.getValue();
    }

    private record UploadCase(String fileName, String contentType, String extension) { }
}
