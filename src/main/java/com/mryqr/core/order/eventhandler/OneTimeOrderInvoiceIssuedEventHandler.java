package com.mryqr.core.order.eventhandler;


import com.mryqr.common.email.MryEmailSender;
import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.OneTimeDomainEventHandler;
import com.mryqr.core.order.domain.Order;
import com.mryqr.core.order.domain.OrderRepository;
import com.mryqr.core.order.domain.event.OrderInvoiceIssuedEvent;
import com.mryqr.core.order.domain.invoice.Invoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.core.common.domain.event.DomainEventType.ORDER_INVOICE_ISSUED;

@Slf4j
@Component
@RequiredArgsConstructor
public class OneTimeOrderInvoiceIssuedEventHandler extends OneTimeDomainEventHandler {
    private final MryEmailSender mryEmailSender;
    private final OrderRepository orderRepository;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == ORDER_INVOICE_ISSUED;
    }

    @Override
    protected void doHandle(DomainEvent domainEvent) {
        OrderInvoiceIssuedEvent theEvent = (OrderInvoiceIssuedEvent) domainEvent;

        Order order = orderRepository.byId(theEvent.getOrderId());
        Invoice invoice = order.getInvoice();
        if (invoice == null) {
            return;
        }

        String emailTo = order.getInvoice().getEmail();
        List<UploadedFile> files = invoice.getFiles();

        mryEmailSender.sendInvoice(emailTo, files);
    }
}
