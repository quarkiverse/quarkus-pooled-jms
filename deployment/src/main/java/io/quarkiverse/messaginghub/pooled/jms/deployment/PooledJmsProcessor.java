package io.quarkiverse.messaginghub.pooled.jms.deployment;

import java.util.function.Function;

import jakarta.inject.Singleton;
import jakarta.jms.ConnectionFactory;

import org.jboss.jandex.DotName;

import io.quarkiverse.jms.spi.deployment.ConnectionFactoryWrapperBuildItem;
import io.quarkiverse.messaginghub.pooled.jms.PooledJmsRecorder;
import io.quarkiverse.messaginghub.pooled.jms.PooledJmsWrapper;
import io.quarkiverse.messaginghub.pooled.jms.transaction.XATransactionSupport;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.ServiceStartBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;

class PooledJmsProcessor {

    private static final String FEATURE = "messaginghub-pooled-jms";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void build(BuildProducer<ReflectiveClassBuildItem> reflectiveClasses) {
        reflectiveClasses.produce(
                ReflectiveClassBuildItem.builder("org.apache.commons.pool2.impl.DefaultEvictionPolicy")
                        .methods(true).fields(false).build());
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void wrap(Capabilities capabilities, PooledJmsRecorder recorder,
            BuildProducer<ConnectionFactoryWrapperBuildItem> wrapper,
            BuildProducer<io.quarkus.jms.spi.deployment.ConnectionFactoryWrapperBuildItem> legacyWrapper,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {
        Function<ConnectionFactory, Object> connectionFactoryWrapper = recorder
                .getWrapper(capabilities.isPresent(Capability.TRANSACTIONS));
        wrapper.produce(new ConnectionFactoryWrapperBuildItem(connectionFactoryWrapper));
        // Retained for extensions that have not yet migrated to the quarkus-jms SPI (e.g. quarkus-qpid-jms)
        legacyWrapper.produce(new io.quarkus.jms.spi.deployment.ConnectionFactoryWrapperBuildItem(connectionFactoryWrapper));
        syntheticBeans.produce(SyntheticBeanBuildItem.configure(PooledJmsWrapper.class)
                .scope(Singleton.class)
                .runtimeValue(recorder.getPooledJmsWrapper(capabilities.isPresent(Capability.TRANSACTIONS)))
                .defaultBean().setRuntimeInit()
                .done());
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    @Consume(ServiceStartBuildItem.class)
    void initializeConnectionFactories(Capabilities capabilities, PooledJmsRecorder recorder) {
        recorder.initializeConnectionFactories(capabilities.isPresent(Capability.TRANSACTIONS));
    }

    @BuildStep
    void unremovableBean(BuildProducer<UnremovableBeanBuildItem> unremovableBeans) {
        if (QuarkusClassLoader.isClassPresentAtRuntime(XATransactionSupport.XA_RECOVERY_REGISTRY_CLASSNAME)) {
            unremovableBeans.produce(UnremovableBeanBuildItem
                    .beanTypes(DotName.createSimple(XATransactionSupport.XA_RECOVERY_REGISTRY_CLASSNAME)));
        }
    }
}
