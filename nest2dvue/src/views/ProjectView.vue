<template>
    <div>
        <div class="split-container">
            <div class="left-side">
                <div class="project-title">
                    <h1>{{ project.name }}</h1>
                </div>
                <div class="card-container">
                    <div v-for="(file, key) in project.files" :key="key" class="card">
                        <img :src="`https://nest2d.online/api/preview/${project.id}/${key}`" alt="SVG Image">
                        <div class="card-content">
                            <h3>{{ file.name }}</h3>
                            <div class="counter">
                                <button :class="{ inactive: fileCounts[key] === 0 }" @click="decrementCount(key)">-
                                </button>
                                <p class="counter-input">{{ fileCounts[key] }}</p>
                                <button @click="incrementCount(key)">+</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="right-side">
                <SvgImage :imageLink="yourSvgImageUrl" placeholderText="Click build to start nest process"/>

                <p class="additional-text">You can click the build button again to look for better results.</p>
                <div class="inputs-column">
                    <label for="width-input">Width</label>
                    <input type="number" id="width-input" class="number-input" value="400"/>

                    <label for="height-input">Height</label>
                    <input type="number" id="height-input" class="number-input" value="570"/>
                </div>
                <div class="buttons-column">
                    <button class="action-button" @click="buildButtonClickHandler" :disabled="isBuilding">Build</button>
                    <button class="action-button-download" @click="downloadFile" :disabled="downloadDisabled">Download
                    </button>
                </div>
            </div>
        </div>
        <div v-if="isBuilding" class="progress-overlay">
            <div class="progress-bar"></div>
        </div>
        <div v-if="showErrorMessage" class="error-dialog">
            <p style="color: red">{{ errorMessage }}</p>
            <button @click="closeErrorDialog">Close</button>
        </div>
    </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import axios from 'axios';
import SvgImage from '@/components/SvgImage.vue';

const yourSvgImageUrl = ref('');
const project = ref({files: {}});
const fileCounts = ref({});
const isBuilding = ref(false);
const downloadDisabled = ref(true);
const nestedId = ref(null);
const errorMessage = ref("");
const showErrorMessage = ref(false);

const closeErrorDialog = async () => {
    showErrorMessage.value = false;
};

const fetchProjectData = async () => {
    try {
        const response = await axios.get(`https://nest2d.online/api/project`);
        project.value = response.data;
        for (const key in response.data.files) {
            fileCounts.value[key] = 0;
        }
    } catch (error) {
        console.error('Error:', error);
        errorMessage.value = error || "Something went wrong. Please try again later.";
        showErrorMessage.value = true;
    }
};

const buildButtonClickHandler = async () => {
    isBuilding.value = true;
    downloadDisabled.value = true;
    errorMessage.value = '';

    let width = document.getElementById("width-input").value;
    let height = document.getElementById("height-input").value;

    if (width === "" || height === "" || isNaN(width) || isNaN(height)) {
        isBuilding.value = false;
        errorMessage.value = "Width and height should be a valid number.";
        showErrorMessage.value = true;
        return;
    }

    let data = {
        project_id: project.value.id,
        file_counts: fileCounts.value,
        plate_width: width, // Assuming you have  input
        plate_height: height, // Assuming you have height input
    };

    try {
        const response = await axios.post(`https://nest2d.online/api/nest`, data);
        console.log(response.data.id);
        downloadDisabled.value = false;
        isBuilding.value = false;
        nestedId.value = response.data.id;
        yourSvgImageUrl.value = `https://nest2d.online/api/nested/${response.data.id}?format=svg`
    } catch (error) {
        isBuilding.value = false;
        errorMessage.value = error.response?.data?.reason || "Something went wrong. Please try again later.";
        showErrorMessage.value = true;
    }
};

const downloadFile = async () => {
    if (!nestedId.value) return;
    try {
        const response = await axios({
            url: `https://nest2d.online/api/nested/${nestedId.value}?format=dxf`,
            method: 'GET',
            responseType: 'blob',
        });

        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'nested.dxf');
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link); // Clean up after downloading
    } catch (error) {
        console.error('Download error:', error);
        // Handle download error appropriately
    }
};

const incrementCount = (key) => {
    fileCounts.value[key]++;
};

const decrementCount = (key) => {
    if (fileCounts.value[key] > 0) {
        fileCounts.value[key]--;
    }
};

onMounted(fetchProjectData);
</script>

<style scoped>
.split-container {
    display: flex;
    height: 100%;
}

.left-side, .right-side {
    flex: 1;
    padding: 20px;
    overflow: auto;
}

.right-side .additional-text {
    display: flex;
    margin: 12px 0 0;
    text-align: center;
    font-size: 12px;
    justify-content: center;
    align-items: center;
    color: white;
    padding: 10px;
    overflow-wrap: break-word;
    white-space: normal;
}

.card-container {
    height: 90vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    overflow: auto;
    overflow-x: hidden;
}

.card {
    width: 100%;
    height: 150px;
    flex-direction: row;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    margin: 10px;
    border-radius: 5px;
    display: flex;
}

.card img {
    aspect-ratio: 1;
    height: 100%;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    border-bottom-right-radius: 5px;
    border-top-right-radius: 5px;
}

.card-content {
    width: 100%;
    padding: 15px;
    display: flex;
    order: 2;
    position: relative;
}

.card h3 {
    margin-top: 0;
}

.counter {
    display: flex;
    align-items: center;
    position: absolute;
    bottom: 10px;
    right: 10px;
    justify-content: space-between;
}

.counter button, .counter .counter-input {
    color: white;
    border: none;
    border-radius: 5px;
    width: 35px;
    height: 35px;
    font-size: 20px;
    text-align: center;
    cursor: pointer;
}

.counter .counter-input {
    background-color: #888888;
}

.counter button {
    background-color: green;
}

.counter button:active {
    background-color: #999999;
}

.counter button.inactive {
    background-color: #ccc;
    cursor: not-allowed;
}

.counter button:focus {
    outline: none;
}

.counter-input {
    background-color: white;
    color: black;
    padding: 0;
    margin: 0 10px;
    border: 1px solid #ccc;
    line-height: 35px;
    cursor: default;
}


.counter-input::-webkit-outer-spin-button,
.counter-input::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
}

.counter-input[type=number] {
    -moz-appearance: textfield;
}

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

.progress-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: rgba(255, 255, 255, 0.8);
}

.progress-bar {
    width: 50%;
    height: 20px;
    background-color: green;
    animation: loading 2s linear infinite;
}

@keyframes loading {
    0% {
        transform: translateX(-100%);
    }
    100% {
        transform: translateX(100%);
    }
}

.error-dialog {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    padding: 20px;
    background-color: black;
    border: 1px solid #ff0000;
    z-index: 100;
}

</style>
