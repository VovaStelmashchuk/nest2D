<template>
    <label class="input">
        <span class="input__label"> {{ label }} </span>
        <span class="input__wrapper">
            <span class="input__hint"> {{ hint }} </span>
            <input
                class="input__value"
                type="file"
                :required="required"
                :accept="accept"
                :multiple="multiple"
                @change="setData"
            />
            <span class="input__text">
                {{ text }}
            </span>
            <span class="input__icon"> </span>
        </span>
    </label>
</template>

<script setup>
import { ref, computed, unref } from 'vue';
const props = defineProps({
    required: {
        type: Boolean,
        default: false,
    },
    multiple: {
        type: Boolean,
        default: false,
    },
    label: {
        type: String,
        default: 'label',
    },
    hint: {
        type: String,
        default: 'hint',
    },
    accept: {
        type: String,
        default: 'image/*',
    },
});

const name = ref('');
const count = ref(0);

const plug = computed(() =>
    unref(props.multiple) ? 'Оберіть файли' : 'Оберіть файл'
);
const text = computed(() => {
    if (unref(count) > 1) {
        return `Файлів : ${unref(count)}`;
    }
    return !!unref(name) ? unref(name) : unref(plug);
});

const setData = (event) => {
    name.value = event.target.files[0].name;
    count.value = event.target.files.length;
};
</script>

<style lang="scss" scoped>
.input {
    display: flex;
    flex-direction: column;
    &__label {
        margin-bottom: 10px;
    }
    &__wrapper {
        position: relative;
        background-color: var(--color-background-soft);
        padding: 10px;
        border: 2px solid var(--color-border);
        border-radius: 5px;
        cursor: pointer;
    }
    &__hint {
        position: absolute;
        left: calc(100% + 10px);
        top: 0;
        font-size: 12px;
    }
    &__value {
        width: 0;
        height: 0;
        pointer-events: none;
        position: absolute;
        top: 0;
        left: 0;
        opacity: 0;
        overflow: hidden;
    }
    &__text {
        line-height: 1.15;
        display: block;
        color: var(--color-text);
        padding-right: 40px;
    }
    &__icon {
        position: absolute;
        top: 0;
        bottom: 0;
        right: 0;
        width: 40px;
        pointer-events: none;

        &::after,
        &::before {
            content: '';
            position: absolute;
            left: 50%;
            top: 50%;
        }

        &::after {
            border-radius: 2px;
            width: 4px;
            height: 20px;
            background-color: #fff;

            transform: translate(-50%, -50%);
        }
        &::before {
            width: 16px;
            height: 16px;
            border-left: 4px solid #fff;
            border-top: 4px solid #fff;
            border-radius: 4px;
            transform: translate(-50%, -50%) rotate(45deg);
        }
    }
}
</style>
