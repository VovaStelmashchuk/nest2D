<template>
    <div class="add-project-page">
        <h1>Add New Project</h1>
        <form @submit.prevent="submitForm">
            <div class="form-group">
                <label for="projectName">Project Name</label>
                <input type="text" id="projectName" v-model="projectName" required>
            </div>
            <div class="form-group">
                <label for="mediaPreview">Media Preview (Image)</label>
                <input type="file" id="mediaPreview" @change="handleMediaPreviewChange" accept="image/*" required>
            </div>
            <div class="form-group">
                <label for="dxfFiles">DXF Files</label>
                <input type="file" id="dxfFiles" @change="handleDXFFilesChange" accept=".dxf" multiple required>
            </div>
            <button type="submit">Upload Project</button>
        </form>
    </div>
    <div v-if="loading" class="progress-bar">
        <div class="progress-bar-fill" :style="{ width: progress + '%' }"></div>
    </div>
</template>

<script setup>
import {ref} from 'vue';
import {API_URL} from "@/constants.js";

const route = useRoute();
const projectName = ref('');
const mediaPreview = ref(null);
const dxfFiles = ref([]);
const loading = ref(false);
const progress = ref(0);

const handleMediaPreviewChange = (event) => {
    mediaPreview.value = event.target.files[0];
};

const handleDXFFilesChange = (event) => {
    dxfFiles.value = Array.from(event.target.files);
};

import axios from 'axios';
import {useRoute} from "vue-router";
import router from "@/router/index.js";

const createProject = async (projectName) => {
    try {
        const response = await axios.post(`${API_URL}/project`, {
            name: projectName
        });
        return response.data;
    } catch (error) {
        console.error('Error creating project:', error);
    }
};

const uploadPreviewImage = async (projectSlug, imageFile) => {
    try {
        const formData = new FormData();
        formData.append('file', imageFile);

        const response = await axios.post(`${API_URL}/project/${projectSlug}/preview`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
        return response.data; // Handle or display the response appropriately
    } catch (error) {
        console.error('Error uploading preview image:', error);
    }
};

const uploadDXFFile = async (projectSlug, dxfFile) => {
    try {
        const formData = new FormData();
        formData.append('file', dxfFile);

        const response = await axios.post(`${API_URL}/files/${projectSlug}/dxf`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error uploading DXF file:', error);
    }
};

const updateProgress = (completed, total) => {
    progress.value = Math.round((completed / total) * 100);
};

const navigateToProject = (projectSlug) => {
    router.push({name: 'ProjectView', params: {slug: projectSlug}});
};

const submitForm = async () => {
    loading.value = true;
    progress.value = 0;
    let projectSlug = '';

    try {
        // Step 1: Create a new project
        updateProgress(1, 4);
        const projectResponse = await axios.post(`${API_URL}/project`, {name: projectName.value});
        projectSlug = projectResponse.data.slug;

        // Step 2: Add preview image
        updateProgress(2, 4);
        let formData = new FormData();
        formData.append('file', mediaPreview.value);
        await axios.post(`${API_URL}/project/${projectSlug}/preview`, formData);

        // Step 3: Add DXF files
        updateProgress(3, 4);
        formData = new FormData();
        dxfFiles.value.forEach(file => {
            formData.append('file', file);
        });
        await axios.post(`${API_URL}/files/${projectSlug}/dxf`, formData);

        updateProgress(4, 4);
        navigateToProject(projectSlug);
    } catch (error) {
        console.error('Submission error:', error);
    } finally {
        loading.value = false;
    }

};
</script>

<style scoped>
.form-group {
    margin-bottom: 20px;
}

label {
    display: block;
    margin-bottom: 5px;
}

input[type="text"], input[type="file"] {
    width: 100%;
    padding: 8px;
    margin-top: 5px;
}

button {
    cursor: pointer;
    padding: 10px 15px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 5px;
}
</style>
