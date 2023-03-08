package io.quarkiverse.messaginghub.pooled.jms.deployment;

import org.jboss.jandex.DotName;

import io.quarkiverse.messaginghub.pooled.jms.PooledJmsRecorder;
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
    void reflective(BuildProducer<ReflectiveClassBuildItem> producer) {
        producer.produce(
                new ReflectiveClassBuildItem(true, false, "org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory"));
        producer.produce(new ReflectiveClassBuildItem(true, false, "org.apache.commons.pool2.impl.DefaultEvictionPolicy"));
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
