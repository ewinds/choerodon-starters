package io.choerodon.asgard.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SagaClientCallback implements SagaClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SagaClient.class);

    @Override
    public List<DataObject.SagaTaskInstanceDTO> pollBatch(String code, String instance, Set<Long> filterIds) {
        LOGGER.warn("error.sagaTaskInstance.poll, code {}, instance {}, filterIds{}", code, instance, filterIds);
        return Collections.emptyList();
    }

    @Override
    public List<DataObject.SagaTaskInstanceDTO> updateStatus(Long id, DataObject.SagaTaskInstanceStatusDTO statusDTO) {
        LOGGER.warn("error.sagaTaskInstance.updateStatus, id {}, status {}", id, statusDTO);
        return Collections.emptyList();
    }

    @Override
    public DataObject.SagaInstance startSaga(String code, DataObject.StartInstanceDTO dto) {
        LOGGER.warn("error.saga.start, code {} , startDto {}", code, dto);
        return null;
    }
}
