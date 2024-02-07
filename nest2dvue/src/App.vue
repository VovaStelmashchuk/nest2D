<template>
    <div class="app-container">
        <AppHeader/>

        <div class="main-content">
            <RouterView/>
        </div>

        <AppFooter/>
    </div>
</template>

<script setup>
import {RouterView} from 'vue-router'
import AppHeader from "@/components/AppHeader.vue";
import AppFooter from "@/components/AppFooter.vue";
</script>

<style scoped>
.app-container {
    display: flex;
    flex-direction: column;
    min-height: calc(100vh - 120px); /* Full viewport height - footer and header */
    width: 100vw;
}

.main-content {
    padding: 0 2rem;
    flex: 1; /* This allows the main content to grow and take available space */
    margin-top: 60px;
}
</style>
