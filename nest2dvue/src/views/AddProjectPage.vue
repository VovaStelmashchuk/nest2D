<template>
    <div class="project">
        <h1 class="project__title">Add New Project</h1>
        <form
            class="project__form form"
            @submit.prevent="submitForm"
        >
            <TextInput
                class="form__item"
                label="Project Name"
                hint="Text"
                v-model="projectName"
                required
            />
            <FileInput
                class="form__item"
                label="Media Preview"
                hint="Image"
                required
                @change="handleMediaPreviewChange"
            />
            <FileInput
                class="form__item"
                label="DXF Files"
                hint="DXF"
                accept=".dxf"
                required
                multiple
                @change="handleDXFFilesChange"
            />
            <button
                class="form__btn"
                type="submit"
            >
                Upload Project
            </button>
        </form>
        <div
            v-if="loading"
            class="project__progress-bar progress-bar"
        >
            <div
                class="progress-bar__inner"
                :style="{ width: progress + '%' }"
            ></div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';
import { API_URL } from '@/constants.js';
import axios from 'axios';
import router from '@/router/index.js';
import TextInput from '@/views/TextInput.vue';
import FileInput from '@/views/FileInput.vue';

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
const updateProgress = (completed, total) => {
    progress.value = Math.round((completed / total) * 100);
};
const navigateToProject = (projectSlug) => {
    router.push({ name: 'ProjectView', params: { slug: projectSlug } });
};

const submitForm = async () => {
    loading.value = true;
    progress.value = 0;
    let projectSlug = '';

    try {
        // Step 1: Create a new project
        updateProgress(1, 4);
        const projectResponse = await axios.post(`${API_URL}/project`, {
            name: projectName.value,
        });
        projectSlug = projectResponse.data.slug;

        // Step 2: Add preview image
        updateProgress(2, 4);
        let formData = new FormData();
        formData.append('file', mediaPreview.value);
        await axios.post(`${API_URL}/project/${projectSlug}/preview`, formData);

        // Step 3: Add DXF files
        updateProgress(3, 4);
        formData = new FormData();
        dxfFiles.value.forEach((file) => {
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

<style lang="scss" scoped>
.project {
    max-width: 840px;
    margin-right: auto;
    margin-left: auto;
    padding: 20px;
    &__title {
        text-align: center;
        margin-bottom: 40px;
    }
    &__form {
        margin-bottom: 40px;
    }
    &__progress-bar {
        height: 20px;
    }
}
.form {
    display: flex;
    flex-direction: column;
    align-items: center;
    &__item {
        width: 300px;
        margin-bottom: 30px;
    }

    &__btn {
        width: 300px;
        border-radius: 5px;
        padding: 10px;
        border: 2px solid hsla(160, 100%, 37%, 1);
        color: hsla(160, 100%, 37%, 1);
        background-color: hsla(160, 100%, 37%, 0.2);
        transition: background-color 0.3s;

        &:hover {
            background-color: hsla(160, 100%, 37%, 0.3);
        }
    }
}

.progress-bar {
    background-color: #f3f3f3;
    border-radius: 10px;
    overflow: hidden;
    &__inner {
        background-color: #4caf50;
        height: 100%;
        border-radius: 10px;
        transition: width 0.4s linear;
    }
}
</style>
