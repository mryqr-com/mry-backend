package com.mryqr.core.plan.query;

public enum QEnabledFeature {
    //基本功能
    QR_NO_EXPIRE,//二维码有效性
    PC_OPERATIONS,//电脑端运营网站
    MOBILE_OPERATIONS,//手机端运营网站
    FORM_CUSTOMIZABLE,//自定义表单
    PLATE_CUSTOMIZABLE,//自定义码牌图案
    CUSTOM_ATTRIBUTE,//自定义实例属性
    CUSTOM_OPS_MENU,//自定义运营菜单
    MEMBER_BATCH_UPLOAD,//批量导入成员数据
    QR_BATCH_UPLOAD, //批量导入实例数据
    GEO_PREVENT_FRAUD,//定位防作假
    PHOTO_PREVENT_FRAUD,//现场上传图片防作假
    PLATE_IMAGE,//生成码牌图片
    VIDEO_AUDIO_ALLOWED,//可上传音视频

    //数据分析
    APP_REPORTING,//统计报表
    KANBAN,//状态看板
    QR_EXCEL_EXPORT,//实例数据导出Excel
    SUBMISSION_EXCEL_EXPORT,//表单数据导出Excel

    //团队协作
    APPROVAL_ENABLED,//分组功能
    FORM_PERMISSION,//表单权限控制
    CONTROL_PERMISSION,//控件权限控制
    OPS_PERMISSION,//运营权限控制
    SUBMISSION_NOTIFY,//提交表单后推送微信消息
    ASSIGNMENT_ENABLED,//任务管理功能

    //品牌展示
    CUSTOM_SUBDOMAIN,//自定义子域名
    CUSTOM_LOGIN_BACKGROUND,//自定义登录页面背景
    CUSTOM_LOGO,//自定义品牌Logo
    HIDE_BOTTOM_MRY_LOGO,//表单底部隐藏码如云Logo
    HIDE_ADS,//不展示广告

    //开发集成
    API_ENABLED,//API集成
    WEBHOOK_ENABLED,//Webhook集成

}
