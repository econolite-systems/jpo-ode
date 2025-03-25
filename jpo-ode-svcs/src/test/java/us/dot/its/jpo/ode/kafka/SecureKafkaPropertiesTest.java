package us.dot.its.jpo.ode.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SecureKafkaPropertiesTest {

  @Test
  void buildSecureKafkaProperties() {
    SecureKafkaProperties secureKafkaProperties = new SecureKafkaProperties();
    secureKafkaProperties.setKey("key123");
    secureKafkaProperties.setSecret("secret322");

    Map<String, Object> expected = new HashMap<>();
    expected.put("ssl.endpoint.identification.algorithm", "https");
    expected.put("security.protocol", "SASL_SSL");
    expected.put("sasl.mechanism", "SCRAM-SHA-512");
    expected.put("ssl.truststore.location", "/home/truststore/ca.p12");
    expected.put("ssl.truststore.type", "PKCS12");
    expected.put("sasl.jaas.config",
        "org.apache.kafka.common.security.scram.ScramLoginModule "
            + "required username=\"key123\" password=\"secret322\";");

    assertEquals(expected, secureKafkaProperties.buildSecureKafkaProperties());
  }
}