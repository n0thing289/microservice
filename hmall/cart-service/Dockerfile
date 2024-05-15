# 基础镜像
FROM openjdk:11.0-jre-buster
# 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 设定环境变量
#ENV APP_HOME=/apps
# 创建容器默认进入的目录
#WORKDIR $APP_HOME
# 拷贝jar包
COPY cart-service.jar /app.jar
# 入口
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=dev"]