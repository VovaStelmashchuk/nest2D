<template>
    <div>
        <div class="split-container">
            <div class="left-side">
                <div class="project-title">
                    <h1>{{ project.name }}</h1>
                </div>
                <div class="card-container">
                    <div v-for="file in project.files" :key="file.id" class="card">
                        <img :src="file.svg_url" alt="SVG Image">
                        <div class="card-content">
                            <h3>{{ file.name }}</h3>
                            <div class="counter">
                                <button :class="{ inactive: fileCounts[file.id] === 0 }"
                                        @click="decrementCount(file.id)">-
                                </button>
                                <p class="counter-input">{{ fileCounts[file.id] }}</p>
                                <button @click="incrementCount(file.id)">+</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="right-side">
                <ControlPanel class="just-card" style="margin-bottom: 10px" :isBuilding="isBuilding"
                              :downloadDisabled="downloadDisabled"
                              @build="buildButtonClickHandler" @download="downloadFile"/>
                <SvgImage class="just-card" :imageLink="yourSvgImageUrl"
                          placeholderText="Click build to start nest process"/>
                <p class="additional-text">You can click the build button again to look for better results.</p>
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
import ControlPanel from '@/components/ControlPanel.vue';
import {API_URL} from "@/constants.js";
import {useRoute} from "vue-router";

const route = useRoute();
const projectId = ref(route.params.id);

const yourSvgImageUrl = ref('');
const project = ref({});
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
        const response = await axios.get(`${API_URL}/project/${projectId.value}`);
        project.value = response.data;
        console.log(response.data.files)
        response.data.files.forEach((file) => fileCounts.value[file.id] = 0)
    } catch (error) {
        console.error('Error:', error);
        errorMessage.value = error || "Something went wrong. Please try again later.";
        showErrorMessage.value = true;
    }
};

const buildButtonClickHandler = async ({width, height}) => {
    isBuilding.value = true;
    downloadDisabled.value = true;
    errorMessage.value = '';

    if (width === "" || height === "" || isNaN(width) || isNaN(height)) {
        isBuilding.value = false;
        errorMessage.value = "Width and height should be a valid number.";
        showErrorMessage.value = true;
        return;
    }

    let data = {
        project_id: project.value.id,
        file_counts: fileCounts.value,
        plate_width: width,
        plate_height: height,
    };

    try {
        const response = await axios.post(`${API_URL}/nest`, data);
        console.log(response.data.id);
        downloadDisabled.value = false;
        isBuilding.value = false;
        nestedId.value = response.data.id;
        yourSvgImageUrl.value = `${API_URL}/nested/${response.data.id}?format=svg`
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
            url: `${API_URL}/nested/${nestedId.value}?format=dxf`,
            method: 'GET',
            responseType: 'blob',
        });

        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'nested.dxf');
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    } catch (error) {
        console.error('Download error:', error);
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

.just-card {
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    border-bottom-right-radius: 5px;
    border-top-right-radius: 5px;
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
