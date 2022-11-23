package io.quarkiverse.messaginghub.pooled.jms.graal;

import javax.management.ObjectName;

import org.apache.commons.pool2.impl.BaseGenericObjectPool;
import org.apache.commons.pool2.impl.BaseObjectPoolConfig;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(BaseGenericObjectPool.class)
public final class CommonsPoolSubstitutions {
    @Substitute
    private ObjectName jmxRegister(final BaseObjectPoolConfig config,
            final String jmxNameBase, String jmxNamePrefix) {
        return null;
    }

    @Substitute
    final void jmxUnregister() {
        // no-op
    }
}
