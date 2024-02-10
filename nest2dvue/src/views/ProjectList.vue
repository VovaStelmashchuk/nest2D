<template>
    <h1>Project List</h1>
    <div class="card-container">
        <div v-for="project in projects" :key="project.id" class="project-card" @click="navigateToProject(project.id)">
            <h2>{{ project.name }}</h2>
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

const navigateToProject = (projectId) => {
    router.push({name: 'ProjectView', params: {id: projectId}});
};

onMounted(async () => {
    try {
        const response = await axios.get(`${API_URL}/projects`);
        projects.value = response.data;
    } catch (error) {
        console.error('Error fetching projects:', error);
    }
});
</script>

<style scoped>
.card-container {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    justify-content: center;
}

.project-card {
    cursor: pointer;
    color: var(--color-text);
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
    padding: 16px;
    width: 300px;
    margin: 8px;
    transition: box-shadow 0.3s;
}

.project-card:hover {
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.5);
}

h2 {
    margin: 8px;
    color: var(--color-text);
    overflow-wrap: break-word;
    white-space: normal;
}
</style>
