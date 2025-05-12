package com.mryqr.core.common.domain.indexedfield;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;

@Getter
@EqualsAndHashCode
public class IndexedValues {
    private IndexedValue f1;
    private IndexedValue f2;
    private IndexedValue f3;
    private IndexedValue f4;
    private IndexedValue f5;
    private IndexedValue f6;
    private IndexedValue f7;
    private IndexedValue f8;
    private IndexedValue f9;
    private IndexedValue f10;
    private IndexedValue f11;
    private IndexedValue f12;
    private IndexedValue f13;
    private IndexedValue f14;
    private IndexedValue f15;
    private IndexedValue f16;
    private IndexedValue f17;
    private IndexedValue f18;
    private IndexedValue f19;
    private IndexedValue f20;

    public void setFieldValue(IndexedField field, IndexedValue value) {
        requireNonNull(field, "Field cannot be null.");
        switch (field) {
            case f1 -> {
                this.f1 = value;
            }
            case f2 -> {
                this.f2 = value;
            }
            case f3 -> {
                this.f3 = value;
            }
            case f4 -> {
                this.f4 = value;
            }
            case f5 -> {
                this.f5 = value;
            }
            case f6 -> {
                this.f6 = value;
            }
            case f7 -> {
                this.f7 = value;
            }
            case f8 -> {
                this.f8 = value;
            }
            case f9 -> {
                this.f9 = value;
            }
            case f10 -> {
                this.f10 = value;
            }
            case f11 -> {
                this.f11 = value;
            }
            case f12 -> {
                this.f12 = value;
            }
            case f13 -> {
                this.f13 = value;
            }
            case f14 -> {
                this.f14 = value;
            }
            case f15 -> {
                this.f15 = value;
            }
            case f16 -> {
                this.f16 = value;
            }
            case f17 -> {
                this.f17 = value;
            }
            case f18 -> {
                this.f18 = value;
            }
            case f19 -> {
                this.f19 = value;
            }
            case f20 -> {
                this.f20 = value;
            }
            default -> throw new NoSuchElementException("No such field: " + field);
        }
    }

    public IndexedValue valueOf(IndexedField field) {
        switch (field) {
            case f1 -> {
                return f1;
            }
            case f2 -> {
                return f2;
            }
            case f3 -> {
                return f3;
            }
            case f4 -> {
                return f4;
            }
            case f5 -> {
                return f5;
            }
            case f6 -> {
                return f6;
            }
            case f7 -> {
                return f7;
            }
            case f8 -> {
                return f8;
            }
            case f9 -> {
                return f9;
            }
            case f10 -> {
                return f10;
            }
            case f11 -> {
                return f11;
            }
            case f12 -> {
                return f12;
            }
            case f13 -> {
                return f13;
            }
            case f14 -> {
                return f14;
            }
            case f15 -> {
                return f15;
            }
            case f16 -> {
                return f16;
            }
            case f17 -> {
                return f17;
            }
            case f18 -> {
                return f18;
            }
            case f19 -> {
                return f19;
            }
            case f20 -> {
                return f20;
            }
            default -> throw new NoSuchElementException("No such field: " + field);
        }
    }

    public boolean isEmpty() {
        return f1 == null &&
                f2 == null &&
                f3 == null &&
                f4 == null &&
                f5 == null &&
                f6 == null &&
                f7 == null &&
                f8 == null &&
                f9 == null &&
                f10 == null &&
                f11 == null &&
                f12 == null &&
                f13 == null &&
                f14 == null &&
                f15 == null &&
                f16 == null &&
                f17 == null &&
                f18 == null &&
                f19 == null &&
                f20 == null;
    }
}
