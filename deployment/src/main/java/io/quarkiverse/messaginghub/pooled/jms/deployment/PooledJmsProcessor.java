package io.quarkiverse.messaginghub.pooled.jms.deployment;

import org.jboss.jandex.DotName;

import io.quarkiverse.messaginghub.pooled.jms.PooledJmsDecorator;
import io.quarkiverse.messaginghub.pooled.jms.PooledJmsRecorder;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
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
    void build(BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClasses) {
        additionalBeans.produce(new AdditionalBeanBuildItem(PooledJmsDecorator.class));
        reflectiveClasses.produce(
                ReflectiveClassBuildItem.builder("org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory")
                        .methods(true).fields(false).build());
        reflectiveClasses.produce(
                ReflectiveClassBuildItem.builder("org.apache.commons.pool2.impl.DefaultEvictionPolicy")
                        .methods(true).fields(false).build());
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    ConnectionFactoryWrapperBuildItem wrap(Capabilities capabilities, PooledJmsRecorder recorder) {
        return new ConnectionFactoryWrapperBuildItem(recorder.getWrapper(capabilities.isPresent(Capability.TRANSACTIONS)));
    }

    @BuildStep
    void unremovableBean(BuildProducer<UnremovableBeanBuildItem> unremovableBeans) {
        unremovableBeans
                .produce(UnremovableBeanBuildItem.beanTypes(DotName.createSimple("org.jboss.tm.XAResourceRecoveryRegistry")));
    }
}
