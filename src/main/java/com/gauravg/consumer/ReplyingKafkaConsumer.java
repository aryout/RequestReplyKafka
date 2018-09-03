package com.gauravg.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import com.gauravg.model.Model;


@Component
public class ReplyingKafkaConsumer {
	 
	 @KafkaListener(topics = "${kafka.topic.request-topic}")
	 @SendTo // 这与过去创建的Kafka消费者一样。唯一的变化是附加了@SendTo注释，此注释用于在响应主题上返回业务结果。
	  public Model listen(Model request) throws InterruptedException {
		 
		 int sum = request.getFirstNumber() + request.getSecondNumber();
		 request.setAdditionalProperty("sum", sum);
		 return request;
		 /*这个消费者用于业务计算，把客户端通过请求传入的两个数字进行相加，然后返回这个请求，通过@SendTo发送到Kafka的响应主题。
		 * */
	  }
	  /*并发消费者
即使你要创建请求主题在三个分区中，三个并发的消费者的响应仍然合并到一个Kafka响应主题，这样，Spring侦听器的容器能够完成匹配相关ID的繁重工作。
*/

}
