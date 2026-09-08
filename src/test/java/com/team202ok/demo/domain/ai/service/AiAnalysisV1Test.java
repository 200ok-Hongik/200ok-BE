package com.team202ok.demo.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team202ok.demo.domain.ai.dto.AiModelResponse;
import com.team202ok.demo.domain.ai.entity.*;
import com.team202ok.demo.domain.ai.repository.*;
import com.team202ok.demo.domain.disposal.entity.TrashCategory;
import com.team202ok.demo.domain.disposal.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AiAnalysisV1Test {
    @Mock ImageUploader imageUploader;
    @Mock ScanResultRepository scanResultRepository;
    @Mock AiScanResultRepository aiScanResultRepository;
    @Mock AiScanDetailRepository aiScanDetailRepository;
    @Mock TrashCategoryRepository trashCategoryRepository;
    @Mock ItemChecklistRepository itemChecklistRepository;
    @Mock AiModelClient aiModelClient;
    @Mock TransactionTemplate transactionTemplate;
    @Spy ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks AiAnalysisServiceImpl service;

    @Test
    void savesEveryObjectAndPreservesFullResponse() throws Exception {
        String raw = """
                {"objects":[
                  {"objectId":"object_1","bbox":{"xMin":0,"yMin":0,"xMax":100,"yMax":100},"finalResult":{"itemCode":"PET_BOTTLE","states":{"hasLabel":null},"source":"VLM_LOW"}},
                  {"objectId":"object_2","bbox":{"xMin":100,"yMin":0,"xMax":200,"yMax":100},"finalResult":{"itemCode":"CAN","states":{"hasLabel":false},"source":"VLM_HIGH"}}
                ],"additionalObjects":[{"candidate":1}],"analysisMetadata":"preserve"}
                """;
        AiModelResponse response = objectMapper.readValue(raw, AiModelResponse.class);
        response.setRawJson(raw);
        var image = new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[]{1});
        when(aiModelClient.requestAnalysis(image)).thenReturn(response);
        for (String code : List.of("PET_BOTTLE", "CAN")) {
            TrashCategory category = mock(TrashCategory.class);
            when(category.getId()).thenReturn(code.equals("CAN") ? 2L : 1L);
            when(trashCategoryRepository.findByCode(code)).thenReturn(Optional.of(category));
        }
        when(imageUploader.upload(image)).thenReturn("image-url");
        when(scanResultRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(aiScanResultRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionTemplate.execute(any())).thenAnswer(i -> ((TransactionCallback<?>) i.getArgument(0)).doInTransaction(null));

        var result = service.analyze(image, 1L);

        assertThat(result.objects()).hasSize(2);
        assertThat(result.additionalObjects()).hasSize(1);
        ArgumentCaptor<AiScanResult> objects = ArgumentCaptor.forClass(AiScanResult.class);
        verify(aiScanResultRepository, times(2)).save(objects.capture());
        assertThat(objects.getAllValues()).extracting(AiScanResult::getObjectId).containsExactly("object_1", "object_2");
        assertThat(objects.getAllValues()).allSatisfy(o -> assertThat(o.getConfidence()).isNull());
        assertThat(objects.getAllValues().get(1).getRawResponse()).contains("VLM_HIGH", "bbox", "false");
        ArgumentCaptor<ScanResult> scan = ArgumentCaptor.forClass(ScanResult.class);
        verify(scanResultRepository).save(scan.capture());
        assertThat(scan.getValue().getAiRawResponse()).isEqualTo(raw);
    }
}
