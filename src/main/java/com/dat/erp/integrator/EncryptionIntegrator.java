package com.dat.erp.integrator;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.Column;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import com.dat.erp.customannotation.encriptedcolumn.Encrypted;

public class EncryptionIntegrator implements Integrator {

    @Override
    public void integrate(
            Metadata metadata,
            SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {

        metadata.getEntityBindings().forEach(entityBinding -> {
            entityBinding.getPropertyClosure().forEach(property -> {
                if (property.getValue() != null &&
                        property.getValue().getType().getName().equals("string") &&
                        property.getPropertyAccessorName().equals("property") &&
                        property.getGetter(entityBinding.getMappedClass()).getMethod()
                                .isAnnotationPresent(Encrypted.class)) {

                    String propertyName = property.getName();
                    String hashColumnName = propertyName + "_hash";

                    // Add hash column to the table
                    entityBinding.getTable().addColumn(new Column(hashColumnName));
                }
            });
        });
    }

    @Override
    public void disintegrate(
            SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
    }
}
