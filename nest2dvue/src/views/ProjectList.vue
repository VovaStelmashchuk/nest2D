<template>
    <div class="card-container">
        <div class="project-card upload-project-card" @click="navigateToUpload">
            <div class="card-content">
                <div class="plus-sign"></div>
                <h2>Upload Your Project</h2>
            </div>
        </div>
        <div v-for="project in projects" :key="project.id" class="project-card"
             @click="navigateToProject(project.slug)">
            <img :src="project.preview" alt="Project Image" class="project-image"/>
            <div class="card-content">
                <h2>{{ project.name }}</h2>
            </div>
        </div>
    </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import axios from 'axios';
import {useRouter} from "vue-router";
import {API_URL} from "@/constants.js";

const projects = ref([]);
const router = useRouter();

const navigateToUpload = () => {
    router.push({name: 'AddProjectPage'});
};

const navigateToProject = (projectSlug) => {
    router.push({name: 'ProjectView', params: {slug: projectSlug}});
};

onMounted(async () => {
    try {
        const response = await axios.get(`${API_URL}/all_projects`);
        projects.value = response.data;
    } catch (error) {
        console.error('Error fetching projects:', error);
    }
});
</script>

<style scoped>
.upload-project-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    color: white;
    cursor: pointer;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
    width: 300px;
    margin: 8px;
    transition: transform 0.3s, box-shadow 0.3s;
    overflow: hidden;
}

.plus-sign {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 6rem;
    height: 200px;
    width: 100%;
}

.plus-sign:before {
    content: '+';
    color: var(--color-text);
}

.card-container {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    justify-content: center;
}

.project-card {
    cursor: pointer;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
    width: 300px;
    margin: 8px;
    transition: transform 0.3s, box-shadow 0.3s;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.project-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.5);
}

.project-image {
    width: 100%;
    height: 200px;
    object-fit: cover;
}

.card-content {
    padding: 16px;
}

h2 {
    margin-bottom: 20px; /* Space below paragraphs */
    color: var(--color-text);
    overflow-wrap: break-word;
    white-space: normal;
}
</style>
