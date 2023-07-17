package me.kalmemarq.common;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class OptionArg<T> {
    protected final String name;
    protected final String alias;
    protected boolean required;
    protected final Class<T> type;
    protected T defaultValue;
    protected T value;

    protected OptionArg(Class<T> clazz, String name, String alias) {
        this.type = clazz;
        this.name = name;
        this.alias = alias;
        this.value = null;
    }

    public OptionArg<T> required() {
        this.required = true;
        return this;
    }

    public OptionArg<T> optional() {
        this.required = false;
        return this;
    }

    public OptionArg<T> defaultsTo(T value) {
        this.defaultValue = value;
        return this;
    }

    public T defaultValue() {
        return this.defaultValue;
    }

    public boolean has() {
        return this.value != null;
    }

    public T value() {
        return this.has() ? this.value : this.defaultValue;
    }

    @SuppressWarnings("unchecked")
    protected void onNotFound() {
        if (this.defaultValue != null) return;

        if (this.type == Boolean.class) {
            this.defaultValue = (T) Boolean.FALSE;
        } else if (this.type == Integer.class) {
            this.defaultValue = (T) Integer.valueOf(0);
        }
    }

    @SuppressWarnings("unchecked")
    protected void parseValues(List<String> values) {
        if (values.isEmpty()) {
            if (this.type == Boolean.class) {
                this.value = (T) Boolean.TRUE;
            }
        } else {
            String value = values.get(0);

            if (this.type.isPrimitive()) {
                try {
                    this.value = (T) this.type.getMethod("valueOf", String.class).invoke(null, value);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                         | NoSuchMethodException | SecurityException e) {
                    System.out.println("(A) Failed to parse arg " + this.name);
                }
            } else {
                try {
                    this.value = (T) this.type.getConstructor(String.class).newInstance(value);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                         | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    System.out.println("(B) Failed to parse arg " + this.name);
                }
            }
        }
    }
}
