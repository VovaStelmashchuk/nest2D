<template>
    <div class="progress-container">
        <div class="progress-bar" :style="{ width: progress + '%' }"></div>
        <div class="timer">{{ timeLeft }} second left</div>
    </div>
</template>

<script>
export default {
    name: 'ProgressBar',
    data() {
        return {
            progress: 0,
            timeLeft: 120,
        };
    },
    mounted() {
        this.startProgress();
    },
    methods: {
        startProgress() {
            const interval = 1000; // Interval in milliseconds (adjust as needed)
            const duration = 2 * 60 * 1000; // Duration of the progress bar in milliseconds
            const step = (interval / duration) * 100;

            const timer = setInterval(() => {
                if (this.progress >= 100) {
                    clearInterval(timer);
                } else {
                    this.progress += step;
                    this.timeLeft -= 1;
                }
            }, interval);
        },
    },
};
</script>

<style scoped>
.timer {
    text-align: center;
    margin-top: 10px;
    color: black;
    font-size: 24px;
}

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
