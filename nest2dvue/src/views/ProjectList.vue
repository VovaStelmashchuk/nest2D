<template>
    <div class="project">
        <div class="project__card project-card">
            <div class="project-card__info project-card-info">
                <span class="project-card-info__plus"></span>
                <h2 class="project-card-info__name">Upload Your Project</h2>
            </div>
            <RouterLink
                class="project-card__link"
                to="/add-project"
            >
                add project
            </RouterLink>
        </div>
        <div
            v-for="project in projects"
            :key="project.id"
            class="project__card project-card"
        >
            <img
                :src="project.preview"
                :alt="`Project ${project.name}`"
                class="project-card__image"
            />
            <div class="project-card__info project-card-info">
                <h2 class="project-card-info__name">
                    {{ project.name }}
                </h2>
            </div>
            <RouterLink
                class="project-card__link"
                :to="`/project/${project.slug}`"
            >
                add project
            </RouterLink>
        </div>
    </div>
</template>

<script setup>
import { onBeforeMount, ref } from 'vue';
import axios from 'axios';
import { API_URL } from '@/constants.js';

const projects = ref([]);

onBeforeMount(async () => {
    try {
        const response = await axios.get(`${API_URL}/all_projects`);
        projects.value = response.data;
    } catch (error) {
        console.error('Error fetching projects:', error);
    }
});
</script>

<style lang="scss" scoped>
.project {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;

    &__card {
        margin: 16px;
        width: 300px;
    }
}
.project-card {
    border-radius: 4px;
    overflow: hidden;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
    transition: transform 0.3s, box-shadow 0.3s;
    position: relative;

    &:hover {
        transform: translateY(-5px);
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.5);
    }
    &__info {
        min-height: 140px;
    }
    &__image {
        position: relative;
        width: 100%;
        height: 200px;
        object-fit: cover;

        &::after {
            content: '';
            position: absolute;
            top: 0;
            right: 0;
            bottom: 0;
            left: 0;
            background: var(--color-background) url('@/img/gear.png') center /
                auto 100% no-repeat;
        }
    }
    &__link {
        position: absolute;
        z-index: 1;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        opacity: 0;
    }
}
.project-card-info {
    padding: 16px;

    &__plus {
        height: 200px;
        width: 100%;
        display: block;
        position: relative;

        &::after,
        &::before {
            content: '';
            position: absolute;
            background-color: rgb(171 171 171);
            transform: translate(-50%, -50%);
            top: 50%;
            left: 50%;

            width: 44px;
            height: 8px;
            border-radius: 4px;
        }

        &::before {
            transform: translate(-50%, -50%) rotate(90deg);
        }
    }
    &__name {
        color: var(--color-text);
    }
}
</style>
