## 项目信息

- [码如云](https://www.mryqr.com)是一个基于二维码的一物一码管理平台，可以为每一件“物品”生成一个二维码，手机扫码即可查看物品信息并发起相关业务操作，操作内容可由你自己定义，典型的应用场景包括固定资产管理、设备巡检以及物品标签等；
- 在技术上，码如云是一个无代码平台，全程采用DDD、整洁架构和事件驱动架构思想完成开发，更多详情可参考笔者的[DDD落地文章系列](https://docs.mryqr.com/ddd-introduction/)；
- 本代码库为码如云的后端代码，技术栈包括：Java 17+，Spring Boot 3+，MongoDB 4+，Redis 6+等，如需码如云前端代码，可通过[码如云官网](https://www.mryqr.com)联系客服获取。

## 如何访问码如云在线服务

- 码如云提供在线SaaS服务，访问地址为：[https://www.mryqr.com](https://www.mryqr.com)。

## 为什么开发码如云

- 请参考作者的文章：[构建自己的软件大厦](https://docs.mryqr.com/build-your-own-software-skyscraper/)。

## 本地运行

- 确保本地已安装Java 17及以上版本，以及Docker；
- 本地启动：`./local-run.sh`，该命令将通过docker-compose自动运行MongoDB和Redis，再启动Spring Boot主程序，启动后访问 http://localhost:8080/about, 如可正常访问则表示启动成功；
- 本地构建：`./ci-build.sh`，该命令将通过docker-compose自动运行MongoDB和Redis，再运行单元测试，API测试以及动态代码检查等构建步骤。

## 常用命令

| 功能                 | 命令                       | 说明                                       |
|--------------------|--------------------------|------------------------------------------|
| 在IntelliJ中打开工程     | `./idea.sh`              | 将自动启动IntelliJ，无需另行在IntelliJ中做导入操作        |
| 本地启动               | `./local-run.sh`         | API端口：8080, 调试端口：5005                    |
| 清空所有本地数据后再启动       | `clear-and-local-run.sh` | API端口：8080, 调试端口：5005                    |
| 本地构建               | `./ci-build.sh`          | 将运行单元测试，API测试以及静态代码检查                    |
| 单独停止docker-compose | `./gradlew composeDown`  | 将清除所有本地数据，包括MongoDB和Redis                |
| 单独启动docker-compose | `./gradlew composeUp`    | 通过docker-compose启动MongoDB和Redis，如已经启动则跳过 |


## IDE配置
- 如果你是在IntelliJ中打开，那么请执行以下操作：在Intelli的设置中，找到Build, Execution, Deployment > Compiler > Java Compiler > Additional command line parameters，然后输入`-parameters`

## 软件协议

本代码库在遵循GPL-3.0协议的基础上，增加了以下协议条款：

- 各企事业单位可免费地将本源代码进行私有化部署以服务于自身业务，但是禁止将所部署的软件（包括直接使用本源代码部署的软件，以及在本源代码基础上修改之后所部署的软件）用于直接商业盈利（包括但不限于将其以付费的方式提供给第三方）。

