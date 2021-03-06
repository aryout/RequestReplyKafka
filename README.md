# RequestReplyKafka
[引用](https://dzone.com/articles/synchronous-kafka-using-spring-request-reply-1)

大家提到Kafka时第一印象就是它是一个快速的异步消息处理系统，不同于通常tomcat之类应用服务器和前端之间的请求/响应方式请求，客户端发出一个请求，必然会等到一个响应，这种方式对Kafka来说好像不适合，因为Kafka是一种事件驱动方式，通过事件才能激活一个响应，但是，问题来了，很多人习惯请求响应模型以后很难接受这种事件响应模型，包括发布订阅模型。

当然，Kafka不是不能实现通常的请求响应模型，只要使用两个Kafka主题，一个是负责请求的主题，另外一个是负责响应的主题，还必须在消息的生产者记录中构建相关ID，将与消息的消费者记录中的ID进行对应关联起来，实际上就是将请求Id和响应Id进行关联。

客户端---->请求的主题 ----消费者处理请求并把结果发送到---->响应主题--->客户端


随着Spring-Kafka最新版本推出(Spring replying kafka 模板)，这种请求-响应模型实现就更加简单了，不需要开发人员自己进行请求Id和响应Id的关联，由Spring kafka模板实现。

下面是本案例的演示架构图，这个案例是实现同步的请求响应模型，以同步行为返回两个数字总和的结果。

![Image](https://dzone.com/storage/temp/8922312-screen-shot-2018-04-22-at-45735-pm.png)

客户端-->请求-->RESTcontroll-->Spring-kafka模板-->Kafka请求主题-->Kafka监听器 
               
客户端<--响应<--RESTcontroll<--Spring-kafka模板<--Kafka响应主题<--Kafka监听器


