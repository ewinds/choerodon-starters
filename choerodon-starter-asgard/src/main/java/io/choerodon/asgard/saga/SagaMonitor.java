package io.choerodon.asgard.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.eureka.CloudEurekaInstanceConfig;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration;
import rx.Observable;
import rx.schedulers.Schedulers;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class SagaMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SagaMonitor.class);

    private ChoerodonSagaProperties choerodonSagaProperties;

    private Optional<EurekaRegistration> eurekaRegistration;

    private SagaClient sagaClient;

    private Executor executor;

    private SagaExecuteObserver observer;

    static final Set<Long> processingIds = Collections.synchronizedSet(new HashSet<>());


    public SagaMonitor(ChoerodonSagaProperties choerodonSagaProperties,
                       SagaClient sagaClient,
                       Executor executor,
                       SagaExecuteObserver observer,
                       Optional<EurekaRegistration> eurekaRegistration) {
        this.choerodonSagaProperties = choerodonSagaProperties;
        this.sagaClient = sagaClient;
        this.executor = executor;
        this.observer = observer;
        this.eurekaRegistration = eurekaRegistration;
    }

    @PostConstruct
    public void start() {
        if (eurekaRegistration.isPresent()) {
            CloudEurekaInstanceConfig cloudEurekaInstanceConfig = eurekaRegistration.get().getInstanceConfig();
            if (cloudEurekaInstanceConfig instanceof EurekaInstanceConfigBean) {
                EurekaInstanceConfigBean eurekaInstanceConfigBean = (EurekaInstanceConfigBean) cloudEurekaInstanceConfig;
                String instance = eurekaInstanceConfigBean.getInstanceId();
                SagaExecuteObserver.invokeBeanMap.entrySet().forEach(i ->
                        Observable.interval(choerodonSagaProperties.getPollInterval(), TimeUnit.SECONDS)
                                .flatMap((Long aLong) -> Observable.from(sagaTaskInstanceDTOS(i.getValue().sagaTask.code(), instance)))
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.from(executor))
                                .subscribe(observer)
                );
            }
        }
    }

    private List<DataObject.SagaTaskInstanceDTO> sagaTaskInstanceDTOS (final String code, final String instance) {
        List<DataObject.SagaTaskInstanceDTO> instanceDTOS =  sagaClient.pollBatch(code, instance, processingIds);
        LOGGER.debug("poll sagaTaskInstances from asgard, time {} instance {} size {}", System.currentTimeMillis(), instance, instanceDTOS.size());
        instanceDTOS.forEach(t -> processingIds.add(t.getId()));
        return instanceDTOS;
    }
}
