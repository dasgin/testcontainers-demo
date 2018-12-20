package com.dasgin.testcontainers.repository;

import com.couchbase.client.java.bucket.BucketType;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.dasgin.testcontainers.configuration.util.CouchbaseConfiguration;
import com.dasgin.testcontainers.domain.Order;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.couchbase.CouchbaseContainer;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(CouchbaseConfiguration.class)
@ContextConfiguration(initializers = CouchbaseRepositoryIT.Initializer.class)
public class CouchbaseRepositoryIT {

    private static final String BUCKET_NAME = "order";
    private static final String COUCHBASE_USER = "Administrator";
    private static final String COUCHBASE_PASSWORD = "administrator";

    @ClassRule
    public static CouchbaseContainer couchbaseContainer = new CouchbaseContainer("couchbase:5.5.0")
            .withClusterAdmin(COUCHBASE_USER, COUCHBASE_PASSWORD)
            .withExposedPorts(8091, 11210, 11207)
            .withNewBucket(DefaultBucketSettings.builder()
                    .enableFlush(true)
                    .name(BUCKET_NAME)
                    .password(COUCHBASE_PASSWORD)
                    .quota(100)
                    .type(BucketType.COUCHBASE)
                    .build());

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues values = TestPropertyValues.of(
                    "spring.couchbase.bootstrap-hosts=" + couchbaseContainer.getContainerIpAddress(),
                    "spring.couchbase.rest.port=" + couchbaseContainer.getMappedPort(8091),
                    "spring.couchbase.bucket.name=" + BUCKET_NAME,
                    "spring.couchbase.bucket.password=" + COUCHBASE_PASSWORD
            );
            values.applyTo(applicationContext);
        }
    }


    @Autowired
    private CouchbaseTemplate couchbaseTemplate;


    @Test
    public void it_should_save_order_to_couchbase(){
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setOrderName("orderName");

        // When
        couchbaseTemplate.insert(order);

        // Then
        assertThat(couchbaseTemplate.findById("1", Order.class).getOrderName()).isEqualTo("orderName");
    }
}

