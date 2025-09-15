package com.dat.erp.interceptors;

import java.lang.reflect.Field;

import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;

import com.dat.erp.customannotation.encriptedcolumn.Encrypted;
import com.dat.erp.utils.CryptoUtils;

public class EncryptionInterceptor implements
        PreInsertEventListener, PreUpdateEventListener, PostLoadEventListener {

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
        processEntity(event.getEntity(), null, event.getPersister().getPropertyNames(), false);
    }

    private boolean processEntity(Object entity, Object[] state, String[] propertyNames, boolean encryptMode) {
        boolean modified = false;
        for (int i = 0; i < propertyNames.length; i++) {
            try {
                Field field = entity.getClass().getDeclaredField(propertyNames[i]);
                field.setAccessible(true);

                if (field.isAnnotationPresent(Encrypted.class)) {
                    Encrypted ann = field.getAnnotation(Encrypted.class);
                    Object value = state[i];
                    if (value != null) {
                        switch (ann.mode()) {
                            case ENCRYPT -> {
                                if (encryptMode) {
                                    String encrypted = CryptoUtils.encrypt(value.toString());
                                    state[i] = encrypted;
                                    field.set(entity, encrypted);
                                } else {
                                    String decrypted = CryptoUtils.decrypt(value.toString());
                                    state[i] = decrypted;
                                    field.set(entity, decrypted);
                                }
                                modified = true;
                            }
                            case HASH -> {
                                if (encryptMode) {
                                    String hashed = CryptoUtils.hash(value.toString());
                                    state[i] = hashed;
                                    field.set(entity, hashed);
                                    modified = true;
                                }
                                // ❌ never decrypt hash
                            }
                        }
                    }
                }
            } catch (NoSuchFieldException ignored) {
                // skip fields not in entity
            } catch (Exception e) {
                throw new RuntimeException("Encryption/Decryption processing failed", e);
            }
        }
        return modified;
    }
}
