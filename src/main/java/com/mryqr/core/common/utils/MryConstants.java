package com.mryqr.core.common.utils;

import java.text.Collator;
import java.time.format.DateTimeFormatter;

import static java.time.ZoneId.systemDefault;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.CHINA;

public class MryConstants {
    public final static String CHINA_TIME_ZONE = "Asia/Shanghai";
    public final static String AUTH_COOKIE_NAME = "mrytoken";
    public static final String NO_TENANT_ID = "NO_TENANT_ID";
    public static final DateTimeFormatter MRY_DATE_TIME_FORMATTER = ofPattern("yyyy-MM-dd HH:mm").withZone(systemDefault());
    public static final DateTimeFormatter MRY_DATE_FORMATTER = ofPattern("yyyy-MM-dd").withZone(systemDefault());
    public static final Collator CHINESE_COLLATOR = Collator.getInstance(CHINA);

    public static final int MAX_PER_PAGE_CONTROL_SIZE = 20;
    public final static int MAX_APP_MANAGER_SIZE = 10;
    public static final int MAX_PER_APP_PAGE_SIZE = 20;
    public static final int MAX_PER_APP_ATTRIBUTE_SIZE = 20;
    public static final int MAX_PER_APP_OPERATION_MENU_SIZE = 20;
    public static final int MAX_PER_APP_NUMBER_REPORT_SIZE = 20;
    public static final int MAX_PER_CHART_REPORT_SIZE = 20;
    public static final int MAX_GROUP_MANAGER_SIZE = 10;
    public static final int MAX_GROUP_HIERARCHY_LEVEL = 5;

    public final static int MIN_SUBDOMAIN_LENGTH = 2;
    public final static int MAX_SUBDOMAIN_LENGTH = 20;
    public final static int MAX_CUSTOM_ID_LENGTH = 50;
    public final static int MAX_GENERIC_NAME_LENGTH = 50;
    public final static int MAX_SHORT_NAME_LENGTH = 10;
    public final static int MAX_PLACEHOLDER_LENGTH = 50;
    public final static int MAX_DIRECT_ATTRIBUTE_VALUE_LENGTH = 100;
    public final static int MAX_URL_LENGTH = 1024;
    public final static int MAX_PARAGRAPH_LENGTH = 50000;

    public final static int MIN_MARGIN = 0;
    public final static int MAX_MARGIN = 100;

    public final static int MIN_PADDING = 0;
    public final static int MAX_PADDING = 100;

    public final static int MIN_BORDER_RADIUS = 0;
    public final static int MAX_BORDER_RADIUS = 100;

    public static final String EVENT_COLLECTION = "event";
    public static final String DEPARTMENT_COLLECTION = "department";
    public static final String DEPARTMENT_HIERARCHY_COLLECTION = "department_hierarchy";
    public static final String GROUP_COLLECTION = "group";
    public static final String GROUP_HIERARCHY_COLLECTION = "group_hierarchy";
    public static final String APP_COLLECTION = "app";
    public static final String APP_MANUAL_COLLECTION = "app_manual";
    public static final String ASSIGNMENT_PLAN_COLLECTION = "assignment_plan";
    public static final String ASSIGNMENT_COLLECTION = "assignment";
    public static final String ORDER_COLLECTION = "order";
    public static final String MEMBER_COLLECTION = "member";
    public static final String QR_COLLECTION = "qr";
    public static final String PLATE_BATCH_COLLECTION = "plate_batch";
    public static final String PLATE_COLLECTION = "plate";
    public static final String SUBMISSION_COLLECTION = "submission";
    public static final String TENANT_COLLECTION = "tenant";
    public static final String VERIFICATION_COLLECTION = "verification";
    public static final String PLATE_TEMPLATE_COLLECTION = "plate_template";
    public static final String SHEDLOCK_COLLECTION = "shedlock";

    public static final String APP_CACHE = "APP";
    public static final String TENANT_APPS_CACHE = "TENANT_APPS";

    public static final String GROUP_CACHE = "GROUP";
    public static final String APP_GROUPS_CACHE = "APP_GROUPS";
    public static final String GROUP_HIERARCHY_CACHE = "GROUP_HIERARCHY";

    public static final String MEMBER_CACHE = "MEMBER";
    public static final String TENANT_MEMBERS_CACHE = "TENANT_MEMBERS";

    public static final String TENANT_DEPARTMENTS_CACHE = "TENANT_DEPARTMENTS";
    public static final String DEPARTMENT_HIERARCHY_CACHE = "DEPARTMENT_HIERARCHY";

    public static final String TENANT_CACHE = "TENANT";
    public static final String API_TENANT_CACHE = "API_TENANT";

    public static final String OPEN_ASSIGNMENT_PAGES_CACHE = "OPEN_ASSIGNMENT_PAGES";

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String ALL = "ALL";

    public static final String REDIS_DOMAIN_EVENT_CONSUMER_GROUP = "domain.event.group";
    public static final String REDIS_WEBHOOK_CONSUMER_GROUP = "webhook.group";
    public static final String REDIS_NOTIFICATION_CONSUMER_GROUP = "notification.group";
}
