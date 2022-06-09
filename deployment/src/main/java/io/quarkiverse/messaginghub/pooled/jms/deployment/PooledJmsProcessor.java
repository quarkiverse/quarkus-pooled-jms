package io.quarkiverse.messaginghub.pooled.jms.deployment;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.transaction.TransactionManager;

import org.jboss.jandex.DotName;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.messaginghub.pooled.jms.JmsPoolXAConnectionFactory;

import io.quarkiverse.messaginghub.pooled.jms.PooledJmsRuntimeConfig;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmoAdaptor;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;

class PooledJmsProcessor {

    private static final String FEATURE = "pooled-jms";
    private static final DotName JMS_POOL_CONNECTION_FACTORY = DotName.createSimple(JmsPoolConnectionFactory.class.getName());

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void generatePooledConnectionFactory(PooledJmsRuntimeConfig config,
            BuildProducer<GeneratedBeanBuildItem> generatedBeanBuildItemBuildProducer) {
        try (ClassCreator c = ClassCreator.builder()
                .classOutput(new GeneratedBeanGizmoAdaptor(generatedBeanBuildItemBuildProducer)).className(
                        JmsPoolConnectionFactory.class.getName() + "Generated")
                .build()) {
            c.addAnnotation(Dependent.class);
            MethodCreator jmsPoolConnectionFactory = c.getMethodCreator("jmsPoolConnectionFactory",
                    JmsPoolConnectionFactory.class, TransactionManager.class);
            jmsPoolConnectionFactory.addAnnotation(Produces.class);
            ResultHandle transactionManagerParam = jmsPoolConnectionFactory.getMethodParam(0);
            ResultHandle result = jmsPoolConnectionFactory
                    .newInstance(MethodDescriptor.ofConstructor(JmsPoolXAConnectionFactory.class));
            jmsPoolConnectionFactory.invokeVirtualMethod(
                    MethodDescriptor.ofMethod(JmsPoolXAConnectionFactory.class, "setTransactionManager", void.class,
                            TransactionManager.class),
                    result,
                    transactionManagerParam);
            jmsPoolConnectionFactory.returnValue(result);
        }

    }
}
