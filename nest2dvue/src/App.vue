<template>
    <div class="app-container">
        <Header />

        <div class="main-content">
            <RouterView />
        </div>

        <Footer />
    </div>
</template>

<script setup>
import { RouterView } from 'vue-router';
import Header from '@/components/Header.vue';
import Footer from '@/components/Footer.vue';
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
