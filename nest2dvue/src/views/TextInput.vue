<template>
    <label class="input">
        <span class="input__label"> {{ label }} </span>
        <span class="input__wrapper">
            <span class="input__hint"> {{ hint }} </span>
            <input
                class="input__value"
                type="text"
                v-model="model"
                :required="required"
            />
        </span>
    </label>
</template>

<script setup>
const model = defineModel();

const props = defineProps({
    required: {
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
});
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
    }
    &__hint {
        position: absolute;
        left: calc(100% + 10px);
        top: 0;
        font-size: 12px;
    }
    &__value {
        display: block;
        width: 100%;
        background-color: var(--color-background-soft);
        padding: 10px;
        box-shadow: none;
        border: 2px solid var(--color-border);
        border-radius: 5px;
        color: var(--color-text);
        font-family: monospace;
    }
}
</style>
