<template>
    <div class="app-container">
        <AppHeader />

        <div class="main-content">
            <RouterView />
        </div>

        <AppFooter />
    </div>
</template>

<script setup>
import { RouterView } from 'vue-router';
import AppHeader from '@/components/AppHeader.vue';
import AppFooter from '@/components/AppFooter.vue';
</script>

<style scoped>
.app-container {
    display: flex;
    flex-direction: column;
    min-height: calc(100vh - 120px);
    margin: 0;
    padding: 0;
}

.main-content {
    padding: 0 2rem;
    flex: 1;
    margin-top: 60px;
}
</style>
