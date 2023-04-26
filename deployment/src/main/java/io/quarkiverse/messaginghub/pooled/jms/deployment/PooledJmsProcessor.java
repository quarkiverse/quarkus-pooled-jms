package io.quarkiverse.messaginghub.pooled.jms.deployment;

import org.jboss.jandex.DotName;

import io.quarkiverse.messaginghub.pooled.jms.PooledJmsDecorator;
import io.quarkiverse.messaginghub.pooled.jms.PooledJmsRecorder;
import io.quarkiverse.messaginghub.pooled.jms.transaction.XATransactionSupport;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
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
    private static final String ACTIVEMQ_CONNECTION_FACTORY = "org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void build(BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClasses) {
        additionalBeans.produce(new AdditionalBeanBuildItem(PooledJmsDecorator.class));

        if (QuarkusClassLoader.isClassPresentAtRuntime(ACTIVEMQ_CONNECTION_FACTORY)) {
            reflectiveClasses.produce(new ReflectiveClassBuildItem(true, false, ACTIVEMQ_CONNECTION_FACTORY));
        }
        reflectiveClasses
                .produce(new ReflectiveClassBuildItem(true, false, "org.apache.commons.pool2.impl.DefaultEvictionPolicy"));
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    ConnectionFactoryWrapperBuildItem wrap(Capabilities capabilities, PooledJmsRecorder recorder) {
        return new ConnectionFactoryWrapperBuildItem(recorder.getWrapper(capabilities.isPresent(Capability.TRANSACTIONS)));
    }

    @BuildStep
    void unremovableBean(BuildProducer<UnremovableBeanBuildItem> unremovableBeans) {
        if (QuarkusClassLoader.isClassPresentAtRuntime(XATransactionSupport.XA_RECOVERY_REGISTRY_CLASSNAME)) {
            unremovableBeans.produce(UnremovableBeanBuildItem
                    .beanTypes(DotName.createSimple(XATransactionSupport.XA_RECOVERY_REGISTRY_CLASSNAME)));
        }
    }
}
