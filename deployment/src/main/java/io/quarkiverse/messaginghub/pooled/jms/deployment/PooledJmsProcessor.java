package io.quarkiverse.messaginghub.pooled.jms.deployment;

import jakarta.inject.Singleton;

import org.jboss.jandex.DotName;

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
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.jms.spi.deployment.ConnectionFactoryWrapperBuildItem;

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
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {
        wrapper.produce(
                new ConnectionFactoryWrapperBuildItem(recorder.getWrapper(capabilities.isPresent(Capability.TRANSACTIONS))));
        syntheticBeans.produce(SyntheticBeanBuildItem.configure(PooledJmsWrapper.class)
                .scope(Singleton.class)
                .runtimeValue(recorder.getPooledJmsWrapper(capabilities.isPresent(Capability.TRANSACTIONS)))
                .defaultBean().setRuntimeInit()
                .done());
    }

    @BuildStep
    void unremovableBean(BuildProducer<UnremovableBeanBuildItem> unremovableBeans) {
        if (QuarkusClassLoader.isClassPresentAtRuntime(XATransactionSupport.XA_RECOVERY_REGISTRY_CLASSNAME)) {
            unremovableBeans.produce(UnremovableBeanBuildItem
                    .beanTypes(DotName.createSimple(XATransactionSupport.XA_RECOVERY_REGISTRY_CLASSNAME)));
        }
    }
}
