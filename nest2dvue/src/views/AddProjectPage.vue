<template>
    <div class="project">
        <h1 class="project__title">Add New Project</h1>
        <form
            class="project__form form"
            @submit.prevent="submitForm"
        >
            <label class="form__item form-item">
                <span class="form-item__label"> Project Name </span>
                <span class="form-item__wrapper">
                    <span class="form-item__hint"> Text </span>
                    <input
                        class="form-item__input"
                        type="text"
                        v-model="projectName"
                        required
                    />
                </span>
            </label>
            <label class="form__item form-item">
                <span class="form-item__label"> Media Preview </span>
                <span class="form-item__wrapper">
                    <span class="form-item__hint"> Image </span>
                    <input
                        class="form-item__input"
                        type="file"
                        @change="handleMediaPreviewChange"
                        accept="image/*"
                        required
                    />
                </span>
            </label>
            <label class="form__item form-item">
                <span class="form-item__label"> DXF Files </span>
                <span class="form-item__wrapper">
                    <span class="form-item__hint"> DXF </span>
                    <input
                        class="form-item__input"
                        type="file"
                        @change="handleDXFFilesChange"
                        accept=".dxf"
                        multiple
                        required
                    />
                </span>
            </label>
            <button
                class="form-item__btn"
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
import router from '@/router/index.js';

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
    &__item {
        width: 300px;
        margin-bottom: 40px;
    }
}
.form-item {
    display: flex;
    flex-direction: column;
    &__wrapper {
        position: relative;
    }
    &__label {
        margin-bottom: 10px;
    }
    &__hint {
        position: absolute;
        left: calc(100% + 10px);
        top: 0;
        font-size: 12px;
    }
    &__input {
        display: block;
        width: 100%;
        background-color: var(--color-background-soft);
        padding: 10px;
        box-shadow: none;
        border: 2px solid var(--color-border);
        border-radius: 5px;
        color: var(--color-text);
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
