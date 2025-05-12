package com.mryqr.support;

import com.mryqr.common.event.DomainEvent;
import com.mryqr.common.event.consume.ConsumingDomainEvent;
import com.mryqr.common.event.consume.DomainEventConsumer;
import com.mryqr.common.event.publish.PublishingDomainEventDao;
import com.mryqr.common.profile.CiProfile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.LinkedList;
import java.util.List;

import static java.lang.ThreadLocal.withInitial;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Aspect
@Component
@CiProfile
@Configuration
@RequiredArgsConstructor
@SuppressWarnings({"unchecked"})
public class SyncConsumeDomainEventInterceptor implements HandlerInterceptor, WebMvcConfigurer {
    private final PublishingDomainEventDao publishingDomainEventDao;
    private final DomainEventConsumer<DomainEvent> domainEventConsumer;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }

    @After("execution(* com.mryqr.common.event.publish.PublishingDomainEventDao.stage(..))")
    public void storeDomainEventIds(JoinPoint joinPoint) {
        if (joinPoint.getArgs()[0] instanceof List<?> events) {
            events.forEach((Object event) -> {
                ThreadLocalDomainEventIdHolder.addEvents((List<DomainEvent>) events);
            });
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        ThreadLocalDomainEventIdHolder.clear();//确保开始处理请求时，holder中没有其它事件ID
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        List<String> eventIds = ThreadLocalDomainEventIdHolder.allEventIds();
        try {
            List<DomainEvent> domainEvents = publishingDomainEventDao.byIds(eventIds);
            domainEvents.forEach(domainEvent -> {
                try {
                    domainEventConsumer.consume(new ConsumingDomainEvent<>(domainEvent.getId(), domainEvent.getType().name(), domainEvent));
                } catch (Throwable t) {
                    log.error("Consume domain event[{}:{}] failed.", domainEvent.getType(), domainEvent.getId(), t);
                }
            });
        } finally {
            ThreadLocalDomainEventIdHolder.remove();
        }
    }

    private static class ThreadLocalDomainEventIdHolder {
        private static final ThreadLocal<LinkedList<String>> THREAD_LOCAL_EVENT_IDS = withInitial(LinkedList::new);

        public static void clear() {
            eventIds().clear();
        }

        public static void remove() {
            THREAD_LOCAL_EVENT_IDS.remove();
        }

        public static List<String> allEventIds() {
            List<String> eventIds = eventIds();
            return isNotEmpty(eventIds) ? List.copyOf(eventIds) : List.of();
        }

        public static void addEvents(List<DomainEvent> events) {
            events.forEach(ThreadLocalDomainEventIdHolder::addEvent);
        }

        public static void addEvent(DomainEvent event) {
            LinkedList<String> eventIds = eventIds();
            eventIds.add(event.getId());
        }

        private static LinkedList<String> eventIds() {
            return THREAD_LOCAL_EVENT_IDS.get();
        }
    }
}
