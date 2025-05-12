package com.mryqr.core.order.domain.invoice;

import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.common.domain.invoice.InvoiceTitle;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

import static java.time.Instant.now;
import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class Invoice {
    private InvoiceTitle title;
    private InvoiceType type;
    private String email;
    private Instant requestedAt;

    private List<UploadedFile> files;
    private Instant issuedAt;

    public Invoice(InvoiceTitle title, InvoiceType type, String email) {
        this.type = type;
        this.title = title;
        this.email = email;
        this.requestedAt = now();
    }

    public void issue(List<UploadedFile> files) {
        this.files = files;
        this.issuedAt = now();
    }

    public boolean isIssued() {
        return issuedAt != null;
    }
}
