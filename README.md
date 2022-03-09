# check_in
接龙管家每日定时打卡
# 请勿用于商业及其他违法用途，仅供试用

## 使用方式

1. 进入https://jielong.co/   接龙管家官方网站，使用微信登录小程序
2. 进入需要每日打卡的项目，通过F12找到请求的**authorization**、**threadId**
3. 其中threadId添加在SimplePostJieLongCheckIn类的常量中
4. authorization添加在ScheduleCheckInTask的静态代码中，put到map中
5. 集成了邮件通知功能，可以取消，如果有需要自行百度如何配置
6. 如果需要自定义打卡填写的内容，浏览器调用接龙打卡接口，复制出请求的json粘贴到SimplePostJieLongCheckIn的json处即可
