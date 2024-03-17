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
    <div v-if="uploadInProgress" class="progress-overlay">
        <ProgressBar/>
    </div>
</template>

<script setup>
import {ref} from 'vue';
import {API_URL} from "@/constants.js";
import ProgressBar from "@/views/ProgressBar.vue";

const projectName = ref('');
const mediaPreview = ref(null);
const dxfFiles = ref([]);
const uploadInProgress = ref(false);

const handleMediaPreviewChange = (event) => {
    mediaPreview.value = event.target.files[0];
};

const handleDXFFilesChange = (event) => {
    dxfFiles.value = Array.from(event.target.files);
};

import axios from 'axios';

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


const submitForm = async () => {
    // Step 1: Create a new project
    const project = await createProject(projectName.value);
    if (!project) return;

    // Assuming project.slug is returned from the createProject call
    const projectSlug = project.slug;

    // Step 2: Upload preview image
    await uploadPreviewImage(projectSlug, mediaPreview.value);

    // Step 3: Upload each DXF file
    for (const dxfFile of dxfFiles.value) {
        await uploadDXFFile(projectSlug, dxfFile);
    }

    // Optionally, navigate the user to a confirmation page or display success message
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
