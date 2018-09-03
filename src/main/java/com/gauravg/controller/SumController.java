package com.gauravg.controller;

import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gauravg.model.Model;

@RestController
public class SumController {
	
	@Autowired
	ReplyingKafkaTemplate<String, Model,Model> kafkaTemplate;
	
	@Value("${kafka.topic.request-topic}")
	String requestTopic;
	
	@Value("${kafka.topic.requestreply-topic}")
	String requestReplyTopic;
	
	@ResponseBody
	@PostMapping(value="/sum",produces=MediaType.APPLICATION_JSON_VALUE,consumes=MediaType.APPLICATION_JSON_VALUE)
	public Model sum(@RequestBody Model request) throws InterruptedException, ExecutionException {
		// create producer record 创建生产者记录
		ProducerRecord<String, Model> record = new ProducerRecord<String, Model>(requestTopic, request);
		// set reply topic in header 在记录头部中设置响应主题
		record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, requestReplyTopic.getBytes()));
		// post in kafka topic 发布到kafka主题中
		RequestReplyFuture<String, Model, Model> sendAndReceive = kafkaTemplate.sendAndReceive(record);

		// confirm if producer produced successfully 确认生产者是否成功生产
		SendResult<String, Model> sendResult = sendAndReceive.getSendFuture().get();
		
		//print all headers 打印结果记录中所有头部信息 会看到Spring自动生成的相关ID，这个ID是由消费端@SendTo 注释返回的值。
		sendResult.getProducerRecord().headers().forEach(header -> System.out.println(header.key() + ":" + header.value().toString()));
		
		// get consumer record 获取消费者记录
		ConsumerRecord<String, Model> consumerRecord = sendAndReceive.get();
		// return consumer value 返回消费者结果
		return consumerRecord.value();		
	}

}
