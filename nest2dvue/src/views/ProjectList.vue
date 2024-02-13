<template>
    <h1>Project List</h1>
    <div class="card-container">
        <div v-for="project in projects" :key="project.id" class="project-card" @click="navigateToProject(project.id)">
            <!-- Image tag with dynamic source -->
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

const navigateToProject = (projectId) => {
    router.push({name: 'ProjectView', params: {id: projectId}});
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
    margin-top: 0;
    overflow-wrap: break-word;
}
</style>
