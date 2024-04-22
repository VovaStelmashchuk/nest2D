<template>
    <div class="app">
        <Header class="app__header" />
        <main class="app__main">
            <RouterView />
        </main>
        <Footer class="app__footer" />
    </div>
</template>

<script setup>
import { RouterView } from 'vue-router';
import Header from '@/components/Header.vue';
import Footer from '@/components/Footer.vue';
</script>

<style lang="scss" scoped>
.app {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    &__header {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        z-index: 1;
    }
    &__main {
        flex-grow: 1;
        margin-top: 100px;
    }
}
</style>
