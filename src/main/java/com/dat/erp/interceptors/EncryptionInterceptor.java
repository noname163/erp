package com.dat.erp.interceptors;

import java.lang.reflect.Field;

import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.Interceptor;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.springframework.stereotype.Component;

import com.dat.erp.customannotation.encriptedcolumn.Encrypted;
import com.dat.erp.utils.CryptoUtils;

@Component
public class EncryptionInterceptor implements
        PreInsertEventListener,
        PreUpdateEventListener,
        PostLoadEventListener,
        Interceptor {

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        return processEntity(event.getEntity(), event.getState(), event.getPersister().getPropertyNames(), true);
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        return processEntity(event.getEntity(), event.getState(), event.getPersister().getPropertyNames(), true);
    }

    @Override
    public void onPostLoad(PostLoadEvent event) {
        // postLoad doesn’t provide state[], only entity
        processEntity(event.getEntity(), null, event.getPersister().getPropertyNames(), false);
    }

    private boolean processEntity(Object entity, Object[] state, String[] propertyNames, boolean encryptMode) {
        boolean modified = false;

        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(Encrypted.class)) {
                Encrypted ann = field.getAnnotation(Encrypted.class);

                try {
                    Object value = field.get(entity);
                    if (value == null)
                        continue;

                    switch (ann.mode()) {
                        case ENCRYPT -> {
                            if (encryptMode) {
                                String encrypted = CryptoUtils.encrypt(value.toString());
                                field.set(entity, encrypted);
                                updateState(state, propertyNames, field.getName(), encrypted);
                            } else {
                                String decrypted = CryptoUtils.decrypt(value.toString());
                                field.set(entity, decrypted);
                                // don’t update state[] after load, keep DB encrypted
                            }
                            modified = true;
                        }
                        case HASH -> {
                            if (encryptMode) {
                                String hashed = CryptoUtils.hash(value.toString());
                                field.set(entity, hashed);
                                updateState(state, propertyNames, field.getName(), hashed);
                                modified = true;
                            }
                            // never decrypt hash
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to process encryption/decryption on field: " + field.getName(),
                            e);
                }
            }
        }

        return modified;
    }

    /**
     * Updates the Hibernate state[] array so that the changes are persisted.
     */
    private void updateState(Object[] state, String[] propertyNames, String fieldName, Object newValue) {
        if (state == null)
            return;

        for (int i = 0; i < propertyNames.length; i++) {
            if (propertyNames[i].equals(fieldName)) {
                state[i] = newValue;
                break;
            }
        }
    }
}
