<template>
    <div>
        <div class="inputs-column">
            <label for="width-input">Width</label>
            <input type="number" id="width-input" class="number-input" v-model="width"/>

            <label for="height-input">Height</label>
            <input type="number" id="height-input" class="number-input" v-model="height"/>

            <label for="width-input">Spacing</label>
            <input type="number" id="spacing-input" class="number-input" v-model="spacingInput"/>

        </div>
        <div class="buttons-column">
            <button class="action-button" @click="onBuildClick" :disabled="isBuilding">Build</button>
            <button class="action-button-download" @click="onDownloadClick" :disabled="downloadDisabled">Download
            </button>
        </div>
    </div>
</template>

<script>
export default {
    props: {
        isBuilding: Boolean,
        downloadDisabled: Boolean,
    },
    emits: ['build', 'download'],
    data() {
        return {
            width: 400,
            height: 570,
            spacingInput: 1.5,
        };
    },
    methods: {
        onBuildClick() {
            this.$emit('build', {width: this.width, height: this.height, spacingInput: this.spacingInput});
        },
        onDownloadClick() {
            this.$emit('download');
        },
    },
};
</script>

<style scoped>
.inputs-column {
    display: flex;
    flex-direction: row;
    gap: 10px;
}

.buttons-column {
    margin-top: 12px;
    display: flex;
    flex-direction: row;
    gap: 24px;
}

.inputs-column label {
    display: block;
    margin-bottom: 5px;
    font-size: 1rem;
    font-weight: bold;
    text-align: center;
}

.number-input {
    padding: 5px;
    border: 1px solid #ccc;
    border-radius: 3px;
    box-sizing: border-box;
}

.action-button {
    padding: 10px;
    background-color: blue;
    color: white;
    border: none;
    line-height: 30px;
    border-radius: 3px;
    cursor: pointer;
    font-weight: bold;
    width: 200px;
}

.action-button-download {
    padding: 10px;
    background-color: green;
    color: white;
    border: none;
    line-height: 30px;
    border-radius: 3px;
    cursor: pointer;
    font-weight: bold;
    width: 200px;
}

.action-button:hover {
    background-color: darkblue;
}

.action-button:disabled {
    background-color: #ccc;
    color: #666;
    cursor: not-allowed;
}

.action-button-download:disabled {
    background-color: #ccc;
    color: #666;
    cursor: not-allowed;
}
</style>
