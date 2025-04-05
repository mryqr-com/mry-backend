package com.mryqr.core.order.eventhandler;


import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.email.MryEmailSender;
import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.core.order.domain.Order;
import com.mryqr.core.order.domain.OrderRepository;
import com.mryqr.core.order.domain.event.OrderInvoiceIssuedEvent;
import com.mryqr.core.order.domain.invoice.Invoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderInvoiceIssuedEventHandler extends AbstractDomainEventHandler<OrderInvoiceIssuedEvent> {
    private final MryEmailSender mryEmailSender;
    private final OrderRepository orderRepository;

    @Override
    public void handle(OrderInvoiceIssuedEvent event) {
        Order order = orderRepository.byId(event.getOrderId());
        Invoice invoice = order.getInvoice();
        if (invoice == null) {
            return;
        }

        String emailTo = order.getInvoice().getEmail();
        List<UploadedFile> files = invoice.getFiles();

        mryEmailSender.sendInvoice(emailTo, files);
    }

    @Override
    public int priority() {
        return 100;
    }
}
