<template>
    <div class="progress-container">
        <div class="progress-bar" :style="{ width: progress + '%' }"></div>
    </div>
</template>

<script>
export default {
    name: 'ProgressBar',
    data() {
        return {
            progress: 0,
        };
    },
    mounted() {
        this.startProgress();
    },
    methods: {
        startProgress() {
            const interval = 100; // Interval in milliseconds (adjust as needed)
            const duration = 2 * 60 * 100; // Duration of the progress bar in milliseconds
            const step = (interval / duration) * 100;

            const timer = setInterval(() => {
                if (this.progress >= 100) {
                    clearInterval(timer);
                } else {
                    this.progress += step;
                }
            }, interval);
        },
    },
};
</script>

<style scoped>
.progress-container {
    width: 100%;
    height: 20px;
    background-color: #eee;
}

.progress-bar {
    height: 20px;
    background-color: green;
    transition: width 0.1s ease-in-out;
}
</style>
