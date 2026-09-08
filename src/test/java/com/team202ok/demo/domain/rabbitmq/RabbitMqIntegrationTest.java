package com.team202ok.demo.domain.rabbitmq;

import com.team202ok.demo.domain.ai.dto.AiRes;
import com.team202ok.demo.domain.ai.service.AiAnalysisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import java.util.List;
import java.time.Duration;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = {"app.rabbitmq.enabled=true", "spring.datasource.url=jdbc:h2:mem:rabbitintegration;MODE=MySQL", "spring.jpa.show-sql=false"})
@EnabledIfEnvironmentVariable(named = "RUN_RABBIT_INTEGRATION", matches = "true")
class RabbitMqIntegrationTest {
    @Autowired AnalysisJobService controller;
    @Autowired RabbitMqProducer producer;
    @MockitoBean AiAnalysisService analysis;

    @Test void realBrokerDeliversJobAndOwnerCanRetrieveResult() throws Exception {
        when(analysis.analyze(any(), eq(42L))).thenReturn(new AiRes.Analyze(123L, List.of(), List.of()));
        var response = controller.submit(new MockMultipartFile("image", "test.png", "image/png", new byte[]{1}), 42L);
        String id = response.jobId();
        assertThat(response.status()).isEqualTo("QUEUED");
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(controller.get(id, 42L).status()).isEqualTo("COMPLETED"));
        assertThat(controller.get(id, 42L).result().get("scanResultId").longValue()).isEqualTo(123L);
        assertThatThrownBy(() -> controller.get(id, 99L)).isInstanceOf(RuntimeException.class);
        producer.send(id);
        // Mockito waits for any unexpected duplicate processing during the observation window.
        verify(analysis, after(1000).times(1)).analyze(any(), eq(42L));
    }
}
