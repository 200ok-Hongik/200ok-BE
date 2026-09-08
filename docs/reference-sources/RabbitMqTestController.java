package com.team202ok.demo.domain;

import com.example.kkikki_be_server.batch.job.TrendAnalysisJob;
import com.example.kkikki_be_server.infra.message.rabbitmq.dto.TrendAnalysisRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("local")
@RequiredArgsConstructor
@RequestMapping("/api/test/rabbitmq")
public class RabbitMqTestController {

    private final RabbitMqProducer rabbitMqProducer;
    private final TrendAnalysisJob trendAnalysisJob;

    @PostMapping("/send")
    public AiMessage<Object> send(@Valid @RequestBody RabbitMqTestRequest request) {
        AiMessage<Object> message = AiMessage.of(request.event(), request.body());
        rabbitMqProducer.send(message);
        return message;
    }

    @PostMapping("/trend-analysis")
    public AiMessage<TrendAnalysisRequest> requestTrendAnalysis(
            @RequestBody TrendAnalysisRequest request
    ) {
        return trendAnalysisJob.execute(request);
    }
}
