package bitronix.tm.integration.cdi;

import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.ejb.MessageDriven;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.AnnotationLiteral;
import javax.transaction.Transactional;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TransactionalCdiExtension implements Extension {

    private static final Logger log = LoggerFactory.getLogger(TransactionalCdiExtension.class);

    AnnotationLiteral<EjbTransactional> EJBTRALITERAL = new AnnotationLiteral<EjbTransactional> () {

        private static final long serialVersionUID = -6529647818427562781L;
    };

    AnnotationLiteral<CdiTransactional> CDITRALITERAL = new AnnotationLiteral<CdiTransactional> () {

        private static final long serialVersionUID = 6942136472219373737L;
    };

    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
        final AnnotatedType<T> annotatedType = pat.getAnnotatedType();
        boolean interceptForEjbTransactions = false;
        boolean interceptForCdiTransactions = false;
        if (annotatedType.isAnnotationPresent(Stateless.class)
                || annotatedType.isAnnotationPresent(Stateful.class)
                || annotatedType.isAnnotationPresent(Singleton.class)
                || annotatedType.isAnnotationPresent(MessageDriven.class)
                ) {
            interceptForEjbTransactions = true;
        }
        for (AnnotatedMethod m: annotatedType.getMethods()) {
            if (m.isAnnotationPresent(Transactional.class)) {
                interceptForCdiTransactions = true;
            }
        }
        if (annotatedType.isAnnotationPresent(Transactional.class)) {
            interceptForCdiTransactions = true;
        }
        if (interceptForCdiTransactions && interceptForEjbTransactions) {
            log.warn("Transactional-Annotation for Ejb ignored {}", annotatedType.getJavaClass().getName());
            interceptForCdiTransactions = false;
        }
        if (interceptForEjbTransactions || interceptForCdiTransactions) {
            final boolean finalInterceptForCdiTransactions = interceptForCdiTransactions;
            pat.setAnnotatedType(new AnnotatedType<T>() {
                @Override
                public Class<T> getJavaClass() {
                    return annotatedType.getJavaClass();
                }

                @Override
                public Set<AnnotatedConstructor<T>> getConstructors() {
                    return annotatedType.getConstructors();
                }

                @Override
                public Set<AnnotatedMethod<? super T>> getMethods() {
                    return annotatedType.getMethods();
                }

                @Override
                public Set<AnnotatedField<? super T>> getFields() {
                    return annotatedType.getFields();
                }

                @Override
                public Type getBaseType() {
                    return annotatedType.getBaseType();
                }

                @Override
                public Set<Type> getTypeClosure() {
                    return annotatedType.getTypeClosure();
                }

                @Override
                public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                    if (finalInterceptForCdiTransactions) {
                        if (annotationType.equals(CdiTransactional.class))
                        return (T) CDITRALITERAL;
                    } else {
                        if (annotationType.equals(EjbTransactional.class))
                            return (T) EJBTRALITERAL;
                    }
                    return annotatedType.getAnnotation(annotationType);
                }

                @Override
                public Set<Annotation> getAnnotations() {
                    Set<Annotation> result = new HashSet<>(annotatedType.getAnnotations());
                    if (finalInterceptForCdiTransactions) {
                        result.add(CDITRALITERAL);
                    } else {
                        result.add(EJBTRALITERAL);

                    }
                    return Collections.unmodifiableSet(result);
                }

                @Override
                public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                    if (finalInterceptForCdiTransactions) {
                        if (annotationType.equals(CdiTransactional.class))
                            return true;
                    } else {
                        if (annotationType.equals(EjbTransactional.class))
                            return true;
                    }
                    return annotatedType.isAnnotationPresent(annotationType);
                }
            });
        }

    }
}
