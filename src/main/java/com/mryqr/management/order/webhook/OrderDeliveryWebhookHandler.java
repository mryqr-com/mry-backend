package com.mryqr.management.order.webhook;

import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.core.order.command.OrderCommandService;
import com.mryqr.core.order.domain.delivery.Carrier;
import com.mryqr.core.order.domain.delivery.Delivery;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.dropdown.DropdownAnswer;
import com.mryqr.core.submission.domain.answer.identifier.IdentifierAnswer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.mryqr.core.common.domain.user.User.NOUSER;
import static com.mryqr.core.order.domain.delivery.Carrier.EMS;
import static com.mryqr.core.order.domain.delivery.Carrier.SHEN_TONG;
import static com.mryqr.core.order.domain.delivery.Carrier.SHUN_FENG;
import static com.mryqr.core.order.domain.delivery.Carrier.YUAN_TONG;
import static com.mryqr.core.order.domain.delivery.Carrier.YUN_DA;
import static com.mryqr.core.order.domain.delivery.Carrier.YUN_DA_56;
import static com.mryqr.core.order.domain.delivery.Carrier.ZHONG_TONG;
import static com.mryqr.core.order.domain.delivery.Carrier.ZHONG_TONG_56;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_DELIVER_EMS_OPTION_ID;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_DELIVER_SHENTONG_OPTION_ID;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_DELIVER_SHUNFENG_OPTION_ID;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_DELIVER_YUANTONG_OPTION_ID;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_DELIVER_YUNDA_EXPRESS_OPTION_ID;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_DELIVER_YUNDA_OPTION_ID;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_DELIVER_ZHONGTONG_EXPRESS_OPTION_ID;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_DELIVER_ZHONGTONG_OPTION_ID;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_REGISTER_DELIVERY_DELIVERY_ID_CONTROL_ID;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_REGISTER_DELIVERY_DELIVER_CONTROL_ID;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_REGISTER_DELIVERY_PAGE_ID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDeliveryWebhookHandler implements OrderWebhookHandler {
    private static final Map<String, Carrier> CARRIERS = Map.of(
            ORDER_DELIVER_EMS_OPTION_ID, EMS,
            ORDER_DELIVER_SHUNFENG_OPTION_ID, SHUN_FENG,
            ORDER_DELIVER_YUANTONG_OPTION_ID, YUAN_TONG,
            ORDER_DELIVER_ZHONGTONG_OPTION_ID, ZHONG_TONG,
            ORDER_DELIVER_ZHONGTONG_EXPRESS_OPTION_ID, ZHONG_TONG_56,
            ORDER_DELIVER_SHENTONG_OPTION_ID, SHEN_TONG,
            ORDER_DELIVER_YUNDA_OPTION_ID, YUN_DA,
            ORDER_DELIVER_YUNDA_EXPRESS_OPTION_ID, YUN_DA_56);

    private final OrderCommandService orderCommandService;

    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload.getPageId().equals(ORDER_REGISTER_DELIVERY_PAGE_ID);
    }

    @Override
    public void handle(BaseSubmissionWebhookPayload payload) {
        String orderId = payload.getQrCustomId();
        Map<String, Answer> answers = payload.allAnswers();

        Carrier carrier = null;
        DropdownAnswer deliverAnswer = (DropdownAnswer) answers.get(ORDER_REGISTER_DELIVERY_DELIVER_CONTROL_ID);
        if (deliverAnswer != null) {
            String deliverOptionId = deliverAnswer.getOptionIds().get(0);
            if (isNotBlank(deliverOptionId)) {
                carrier = CARRIERS.get(deliverOptionId);
            }
        }

        String deliveryId = null;
        IdentifierAnswer deliveryIdAnswer = (IdentifierAnswer) answers.get(ORDER_REGISTER_DELIVERY_DELIVERY_ID_CONTROL_ID);
        if (deliveryIdAnswer != null) {
            deliveryId = deliveryIdAnswer.getContent();
        }

        Delivery delivery = Delivery.builder()
                .carrier(carrier)
                .deliveryOrderId(deliveryId)
                .build();
        orderCommandService.updateDelivery(orderId, delivery, NOUSER);
        log.info("Updated delivery info for order[{}] .", orderId);
    }
}
