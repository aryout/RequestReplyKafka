package com.gauravg.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.gauravg.model.Model;

@Configuration
public class KafkaConfig {
	
	  @Value("${kafka.bootstrap-servers}")
	  private String bootstrapServers;
	  
	  @Value("${kafka.topic.requestreply-topic}")
	  private String requestReplyTopic;
	  
	  @Value("${kafka.consumergroup}")
	  private String consumerGroup;

	// 配件：kafka生产者的Kafka配置Standard KafkaProducer settings - specifying brokerand serializer
	  @Bean
	  public Map<String, Object> producerConfigs() {
	    Map<String, Object> props = new HashMap<>();
	    // list of host:port pairs used for establishing the initial connections to the Kakfa cluster
	    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
	        bootstrapServers);
	    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
	        StringSerializer.class);
	    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
	    return props;
	  }

	@Bean // 消费者工厂
	public ProducerFactory<String,Model> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}
	  
	  @Bean
	  public Map<String, Object> consumerConfigs() {
	    Map<String, Object> props = new HashMap<>();
	    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServers);
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, "helloworld");
	    return props;
	  }

	// 配件：生产者工厂Default Producer Factory to be used in ReplyingKafkaTemplate
	@Bean // 生产者工厂
	public ConsumerFactory<String, Model> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(),new StringDeserializer(),new JsonDeserializer<>(Model.class));
	}


	// 配件：监听器容器Listener Container to be set up in ReplyingKafkaTemplate
	@Bean // 消息监听器容器，参数是：消费者工厂和容器配置
	public KafkaMessageListenerContainer<String, Model> replyContainer(ConsumerFactory<String, Model> cf) {
		ContainerProperties containerProperties = new ContainerProperties(requestReplyTopic);
		return new KafkaMessageListenerContainer<>(cf, containerProperties);
	}

	/*配置核心的ReplyingKafkaTemplate类,这个类继承了 KafkaTemplate 提供请求/响应的的行为*/
	@Bean // 参数是：生产者工厂和(并发)消息监听器容器
	public ReplyingKafkaTemplate<String, Model, Model> replyKafkaTemplate(ProducerFactory<String, Model> pf, KafkaMessageListenerContainer<String, Model> container){
		return new ReplyingKafkaTemplate<>(pf, container);
	}

	  @Bean // 非请求响应的模板类，参数只需要生产者工厂
	  public KafkaTemplate<String, Model> kafkaTemplate() {
	    return new KafkaTemplate<>(producerFactory());
	  }
	  
	  @Bean // (并发)监听器容器工厂，生产 “并发消息监听器容器”
	  public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Model>> kafkaListenerContainerFactory() {
	    ConcurrentKafkaListenerContainerFactory<String, Model> factory = new ConcurrentKafkaListenerContainerFactory<>();
	    factory.setConsumerFactory(consumerFactory()); // 需要生产者工厂
		  // NOTE - set up of reply template 设置响应模板
		  // 这是必须的，因为消费者需要将计算结果放入到Kafka的响应主题
	    factory.setReplyTemplate(kafkaTemplate()); // 需要消费者工厂
	    return factory;
	  }
	  
}

